package io.soffa.foundation.service.pubsub;

import io.nats.client.*;
import io.nats.client.impl.NatsMessage;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.exceptions.ConfigurationException;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.messages.MessageHandler;
import io.soffa.foundation.messages.PubSubClient;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.service.PlatformAuthManager;
import io.soffa.foundation.service.pubsub.nats.NatsClientConfig;
import io.soffa.foundation.service.pubsub.nats.NatsConfig;
import io.soffa.foundation.service.state.DatabasePlane;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.soffa.foundation.Constants.OPERATION;
import static io.soffa.foundation.metrics.CoreMetrics.*;

public class NatsClient implements PubSubClient {

    private static final Logger LOG = Logger.get(NatsClient.class);
    private final Map<String, Connection> clients = new HashMap<>();
    private final Map<String, String> broadcastSubjects = new HashMap<>();
    private final Map<String, JetStream> streams = new HashMap<>();
    private final String applicationName;
    private final MetricsRegistry metricsRegistry;
    private final DatabasePlane dbPlane;
    private final PlatformAuthManager authManager;
    public static final String DEFAULT_CLIENT = "default";
    private final List<Subscription> subscriptions = new ArrayList<>();
    private boolean ready;


    public NatsClient(
        PlatformAuthManager authManager,
        String applicationName,
        NatsConfig config,
        MessageHandler handler,
        MetricsRegistry metricsRegistry,
        DatabasePlane dbPlane) {

        this.ready = false;
        this.authManager = authManager;
        this.metricsRegistry = metricsRegistry;
        this.applicationName = applicationName;
        this.dbPlane = dbPlane;

        if (!config.hasDefaultClient()) {
            throw new ConfigurationException("Nats client configuration is missing default client");
        }

        configure(config, handler);
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @SuppressWarnings("PMD")
    private void configure(NatsConfig config, MessageHandler handler) {
        for (Map.Entry<String, NatsClientConfig> cc : config.getClients().entrySet()) {
            String clientId = cc.getKey();
            NatsClientConfig clientConfig = cc.getValue();
            if (TextUtil.isEmpty(clientConfig.getAddresses())) {
                throw new ConfigurationException("Nats client configuration is missing addresses for client " + clientId);
            }
            String[] addresses = clientConfig.getAddresses().split(",");
            try {
                Options o = new Options.Builder().servers(addresses).maxReconnects(-1).build();
                Connection connexion = Nats.connect(o);
                this.clients.put(clientId, connexion);
                this.subsribe(clientId, applicationName, clientConfig.getBroadcast(), handler);
                LOG.info("Connected to NATS servers: %s", clientConfig.getAddresses());
            } catch (Exception e) {
                for (Connection value : this.clients.values()) {
                    try {
                        value.close();
                    } catch (InterruptedException ignore) {
                        LOG.warn("Error while closing NATS connection");
                    }
                }
                throw new TechnicalException(e, "Unable to connect to NATS @ %s", clientConfig.getAddresses());
            }
        }
    }

    private Map<String, Object> createTags(@Nullable String subject, io.soffa.foundation.messages.Message message) {
        Map<String, Object> tags = new HashMap<>();
        if (subject != null) {
            tags.put(OPERATION, message.getOperation());
        }
        tags.put(OPERATION, message.getOperation());
        return tags;
    }

    @Override
    public <T> CompletableFuture<T> request(String subject, io.soffa.foundation.messages.Message message, Class<T> responseClass, String client) {
        return metricsRegistry.track(NATS_REQUEST,
            createTags(subject, message),
            () -> {
                LOG.debug("Sending request #%s to %s", message.getId(), subject);
                return getClient(client).request(createNatsMessage(subject, message))
                    .thenApply(msg -> JsonUtil.deserialize(msg.getData(), responseClass));
            });
    }

    @Override
    public void publish(String subject, io.soffa.foundation.messages.Message message, String client) {
        String sub = getSubject(subject);
        metricsRegistry.track(
            NATS_PUBLISH,
            createTags(sub, message),
            () -> getClient(client).publish(createNatsMessage(sub, message))
        );
    }

    private String getSubject(String subject) {
        if (TextUtil.isEmpty(subject)) {
            return applicationName;
        }
        return subject;
    }

    private Connection getClient(String id) {
        if (TextUtil.isEmpty(id)) {
            return getClient(DEFAULT_CLIENT);
        }
        if (!clients.containsKey(id)) {
            throw new ConfigurationException("No client registered with id: " + id);
        }
        return clients.get(id);
    }

    private JetStream getStream(String id) {
        if (TextUtil.isEmpty(id)) {
            return getStream(DEFAULT_CLIENT);
        }
        if (!streams.containsKey(id)) {
            throw new ConfigurationException("No jetsteam client registered with id: " + id);
        }
        return streams.get(id);
    }

    @Override
    public void broadcast(io.soffa.foundation.messages.Message message, String client) {
        if (TextUtil.isEmpty(client)) {
            broadcast(message, DEFAULT_CLIENT);
            return;
        }
        if (message.getContext() == null) {
            message.setContext(RequestContextHolder.getOrCreate());
        }
        final String broadcastSubjet = broadcastSubjects.get(client);
        if (TextUtil.isEmpty(broadcastSubjet)) {
            throw new ConfigurationException("No broadcast subject configured for client: " + client);
        }
        //noinspection Convert2Lambda
        metricsRegistry.track(
            NATS_BROADCAST,
            createTags(null, message),
            new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    getClient(client).publish(createNatsMessage(broadcastSubjet, message));
                    /*if (ack.hasError()) {
                        throw new TechnicalException(ack.getError());
                    }*/
                }
            });
    }

    private NatsMessage createNatsMessage(String subject, io.soffa.foundation.messages.Message message) {
        return new NatsMessage(subject, "", JsonUtil.serialize(message).getBytes(StandardCharsets.UTF_8));
    }

    private void setReady() {
        ready = true;
        LOG.info("NATS client is now ready for business");
    }

    private void subsribe(final String clientId, final String subject, final String queue, final MessageHandler handler) {
        synchronized (dbPlane) {
            if (dbPlane.isReady()) {
                doSubsribe(clientId, subject, queue, handler);
                setReady();
                return;
            }
            final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            LOG.info("Waiting from DatabasePlane before subscribing to %s", subject);
            executor.scheduleAtFixedRate(() -> {
                if (dbPlane.isReady()) {
                    if (!ready) {
                        doSubsribe(clientId, subject, queue, handler);
                    }
                    setReady();
                    executor.shutdown();
                }
            }, 1000, 500, TimeUnit.MILLISECONDS);
        }
    }

    @SneakyThrows
    private void doSubsribe(String clientId, String subject, String broadcast, MessageHandler handler) {

        LOG.info("Configuring subscription to %s", subject);

        NatsMessageHandler h = new NatsMessageHandler(metricsRegistry, authManager, handler, true);
        @SuppressWarnings("PMD")
        Connection cli = getClient(clientId);
        Dispatcher dispatcher = cli.createDispatcher(h);
        dispatcher.subscribe(subject, subject + "-group");

        if (TextUtil.isNotEmpty(broadcast)) {

            JetStreamOptions jso = JetStreamOptions.defaultOptions();
            JetStream stream = cli.jetStream(jso);
            streams.put(clientId, stream);
            broadcastSubjects.put(clientId, broadcast);

            PushSubscribeOptions so = PushSubscribeOptions.builder()
                .durable(applicationName)
                .build();
            subscriptions.add(getStream(clientId).subscribe(
                broadcast, dispatcher, h, true, so
            ));

            /*
            StreamConfiguration.Builder scBuilder = StreamConfiguration.builder()
                .name(clientConfig.getBroadcast());
             */
            /*try {
                Subscription sub = stream.subscribe(clientConfig.getBroadcast());
                sub.unsubscribe();
            } catch (IllegalStateException e) {
                LOG.warn(e.getMessage());
                scBuilder.addSubjects(clientConfig.getBroadcast());
            }*/
            /*
                    JetStreamManagement jsm = connexion.jetStreamManagement();
                    jsm.addStream(scBuilder.build());
             */


        }

    }

    @PreDestroy
    @SuppressWarnings("PMD")
    public void cleanup() {
        for (Subscription subscription : subscriptions) {
            try {
                subscription.unsubscribe();
            } catch (Exception e) {
                LOG.debug("Unsubscription failed: %s", e.getMessage());
            }
        }
        for (Connection connection : clients.values()) {
            try {
                connection.close();
            } catch (Exception e) {
                LOG.error("Unable to close NATS connection", e);
            }
        }
    }


}
