package io.soffa.foundation.service;

import io.nats.client.*;
import io.nats.client.api.PublishAck;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.impl.NatsMessage;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.exceptions.ConfigurationException;
import io.soffa.foundation.exceptions.FunctionalException;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.messages.MessageHandler;
import io.soffa.foundation.messages.PubSubClient;
import io.soffa.foundation.metrics.CoreMetrics;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.service.pubsub.nats.NatsClientConfig;
import io.soffa.foundation.service.pubsub.nats.NatsConfig;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.soffa.foundation.Constants.OPERATION;
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

    @SuppressWarnings("PMD")
    public NatsClient(
        PlatformAuthManager authManager,
        String applicationName,
        NatsConfig config,
        MessageHandler handler,
        MetricsRegistry metricsRegistry) {

        this.authManager = authManager;
        this.metricsRegistry = metricsRegistry;
        this.applicationName = applicationName;

        if (!config.hasDefaultClient()) {
            throw new ConfigurationException("Nats client configuration is missing default client");
        }

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

                JetStreamOptions jso = JetStreamOptions.defaultOptions();
                JetStream stream = connexion.jetStream(jso);
                this.streams.put(clientId, stream);


                if (TextUtil.isNotEmpty(clientConfig.getBroadcast())) {

                    StreamConfiguration.Builder scBuilder = StreamConfiguration.builder()
                        .name(IdGenerator.shortUUID(applicationName));
                    try {
                        Subscription sub = stream.subscribe(clientConfig.getBroadcast());
                        sub.unsubscribe();
                    } catch (IllegalStateException e) {
                        LOG.warn(e.getMessage());
                        scBuilder.addSubjects(clientConfig.getBroadcast());
                    }

                    broadcastSubjects.put(clientId, clientConfig.getBroadcast());
                    JetStreamManagement jsm = connexion.jetStreamManagement();
                    jsm.addStream(scBuilder.build());
                }

                this.subsribe(applicationName, clientConfig.getBroadcast(), handler);

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
                //EL
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
                    PublishAck ack = getStream(client).publish(createNatsMessage(broadcastSubjet, message));
                    if (ack.hasError()) {
                        throw new TechnicalException(ack.getError());
                    }
                }
            });
    }

    private NatsMessage createNatsMessage(String subject, io.soffa.foundation.messages.Message message) {
        return new NatsMessage(subject, "", JsonUtil.serialize(message).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public final void subsribe(String subject, String queue, MessageHandler handler) {

        io.nats.client.MessageHandler localHandler = (msg) -> {
            try {
                io.soffa.foundation.messages.Message message = JsonUtil.deserialize(msg.getData(), io.soffa.foundation.messages.Message.class);
                if (message.getContext() != null) {
                    authManager.process(message.getContext());
                    RequestContextHolder.set(message.getContext());
                }
                Optional<Object> response = handler.handle(message);
                if (TextUtil.isNotEmpty(msg.getReplyTo())) {
                    byte[] responseData = response.map(o -> JsonUtil.serialize(o).getBytes(StandardCharsets.UTF_8)).orElse(null);
                    msg.getConnection().publish(msg.getReplyTo(), responseData);
                } else {
                    msg.ack();
                }
                metricsRegistry.increment(CoreMetrics.NATS_EVENT_PROCESSED);
            } catch (FunctionalException e) {
                LOG.warn(TextUtil.format("Message %s was skipped due to a functionnal error", msg.getSID()), e);
                LOG.warn(e.getMessage(), e);
                msg.ack();
                metricsRegistry.increment(CoreMetrics.NATS_EVENT_SKIPPED);
            } catch (Exception e) {
                LOG.error("Nats avent handling failed with error", e);
                msg.nak();
                metricsRegistry.increment(CoreMetrics.NATS_EVENT_PROCESSING_FAILED);
            }
        };

        Dispatcher dispatcher = getClient(DEFAULT_CLIENT).createDispatcher(localHandler);
        dispatcher.subscribe(subject);

        if (TextUtil.isNotEmpty(queue)) {
            PushSubscribeOptions so = PushSubscribeOptions.builder()
                .durable(applicationName)
                .build();
            try {
                getStream(DEFAULT_CLIENT).subscribe(queue, dispatcher, localHandler, false, so);
            } catch (JetStreamApiException | IOException e) {
                throw new TechnicalException(e.getMessage(), e);
            }
        }

    }

    @PreDestroy
    @SuppressWarnings("PMD")
    public void cleanup() {
        for (Connection connection : clients.values()) {
            try {
                connection.close();
            } catch (Exception e) {
                LOG.error("Unable to close NATS connection", e);
            }
        }
    }


}
