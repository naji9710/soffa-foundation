package io.soffa.foundation.service.pubsub;

import com.google.common.eventbus.Subscribe;
import io.nats.client.*;
import io.nats.client.api.PublishAck;
import io.nats.client.api.StreamConfiguration;
import io.soffa.foundation.application.EventBus;
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
import io.soffa.foundation.service.data.DatabaseReadyEvent;
import io.soffa.foundation.service.pubsub.nats.NatsClientConfig;
import io.soffa.foundation.service.pubsub.nats.NatsConfig;
import io.soffa.foundation.service.state.DatabasePlane;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.soffa.foundation.metrics.CoreMetrics.*;

public class NatsClient implements PubSubClient {

    private static final Logger LOG = Logger.get(NatsClient.class);
    private final Map<String, Connection> clients = new HashMap<>();
    private final Map<String, String> broadcastSubjects = new HashMap<>();
    private final Map<String, JetStream> streams = new HashMap<>();
    private final String applicationName;
    private final MetricsRegistry metricsRegistry;
    private final PlatformAuthManager authManager;
    public static final String DEFAULT_CLIENT = "default";
    private final List<Subscription> subscriptions = new ArrayList<>();
    private final AtomicBoolean ready = new AtomicBoolean(false);
    private final MessageHandler handler;
    private final NatsConfig config;

    public NatsClient(
        PlatformAuthManager authManager,
        String applicationName,
        NatsConfig config,
        MessageHandler handler,
        MetricsRegistry metricsRegistry,
        DatabasePlane dbPlane) {

        if (!config.hasDefaultClient()) {
            throw new ConfigurationException("Nats client configuration is missing default client");
        }

        EventBus.register(this);
        if (dbPlane.isReady()) {
            ready.set(true);
        }

        this.handler = handler;
        this.config = config;
        this.authManager = authManager;
        this.metricsRegistry = metricsRegistry;
        this.applicationName = applicationName;

        configureClients();
    }

    @Subscribe
    protected void onDatabaseReady(@NonNull DatabaseReadyEvent event) {
        EventBus.unregister(this);
        LOG.info("DB is ready, configuring nats clients...");
        try {
            configureSubscriptions(this.config, handler);
            setReady();
        } catch (Exception e) {
            LOG.error("Configuration failed", e);
        }
    }

    @Override
    public boolean isReady() {
        return ready.get();
    }

    private void configureClients() {
        for (Map.Entry<String, NatsClientConfig> cc : config.getClients().entrySet()) {
            String clientId = cc.getKey();
            NatsClientConfig clientConfig = cc.getValue();
            if (TextUtil.isEmpty(clientConfig.getAddresses())) {
                throw new ConfigurationException("Nats client configuration is missing addresses for client " + clientId);
            }
            String[] addresses = clientConfig.getAddresses().split(",");
            try {
                Options o = new Options.Builder().servers(addresses).maxReconnects(-1).build();
                @SuppressWarnings("PMD.CloseResource")
                Connection connexion = Nats.connect(o);
                this.clients.put(clientId, connexion);

                String broadcast = clientConfig.getBroadcast();
                if (TextUtil.isNotEmpty(broadcast)) {
                    JetStreamOptions jso = JetStreamOptions.defaultOptions();
                    JetStream stream = connexion.jetStream(jso);
                    streams.put(clientId, stream);
                    broadcastSubjects.put(clientId, broadcast);

                    StreamConfiguration.Builder scBuilder = StreamConfiguration.builder()
                        .name(broadcast);
                    try {
                        Subscription sub = stream.subscribe(broadcast);
                        sub.unsubscribe();
                    } catch (IllegalStateException e) {
                        LOG.warn(e.getMessage());
                        scBuilder.addSubjects(broadcast);
                    }
                    JetStreamManagement jsm = connexion.jetStreamManagement();
                    try {
                        jsm.addStream(scBuilder.build());
                    } catch (JetStreamApiException ignore) {
                        LOG.warn("Stream %s already configured", broadcast);
                    }
                }

                LOG.info("Connected to NATS servers: %s", clientConfig.getAddresses());
            } catch (Exception e) {
                for (@SuppressWarnings("PMD.CloseResource") Connection value : this.clients.values()) {
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

    @SuppressWarnings("PMD")
    private void configureSubscriptions(NatsConfig config, MessageHandler handler) {
        for (Map.Entry<String, NatsClientConfig> cc : config.getClients().entrySet()) {
            this.subsribe(cc.getKey(), applicationName, cc.getValue().getBroadcast(), handler);
        }
    }

    @Override
    public <T> CompletableFuture<T> request(String subject, io.soffa.foundation.messages.Message message, Class<T> responseClass, String client) {
        return metricsRegistry.track(NATS_REQUEST,
            message.getTags(subject),
            () -> {
                LOG.debug("Sending request #%s to %s", message.getId(), subject);
                return getClient(client).request(NatsUtil.createNatsMessage(subject, message))
                    .thenApply(msg -> JsonUtil.deserialize(msg.getData(), responseClass));
            });
    }

    @Override
    public void publish(String subject, io.soffa.foundation.messages.Message message, String client) {
        String sub = getSubject(subject);
        metricsRegistry.track(
            NATS_PUBLISH,
            message.getTags(sub),
            () -> getClient(client).publish(NatsUtil.createNatsMessage(sub, message))
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
            throw new ConfigurationException("No nats client registered with id: " + id);
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
            message.getTags(null),
            new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    PublishAck ack = getStream(client).publish(NatsUtil.createNatsMessage(broadcastSubjet, message));
                    if (ack.hasError()) {
                        throw new TechnicalException(ack.getError());
                    }
                }
            });
    }


    private void setReady() {
        ready.set(true);
        LOG.info("NATS client is now ready for business");
    }

    @SneakyThrows
    private void subsribe(String clientId, String subject, String broadcast, MessageHandler handler) {
        LOG.info("Configuring subscription to %s", subject);

        NatsMessageHandler h = new NatsMessageHandler(metricsRegistry, authManager, handler, true);
        @SuppressWarnings("PMD")
        Connection cli = getClient(clientId);
        Dispatcher dispatcher = cli.createDispatcher(h);
        dispatcher.subscribe(subject, subject + "-group");

        if (TextUtil.isNotEmpty(broadcast)) {
            PushSubscribeOptions so = PushSubscribeOptions.builder()
                .durable(applicationName)
                .build();
            subscriptions.add(getStream(clientId).subscribe(
                broadcast, dispatcher, h, true, so
            ));
        }
    }

    @PreDestroy
    @SuppressWarnings("PMD")
    public void cleanup() {
        NatsUtil.unsubscribe(subscriptions);
        NatsUtil.close(clients.values());
    }


}
