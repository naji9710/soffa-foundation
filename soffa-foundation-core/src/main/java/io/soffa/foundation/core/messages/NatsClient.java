package io.soffa.foundation.core.messages;

import io.nats.client.*;
import io.nats.client.api.PublishAck;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.impl.NatsMessage;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.exceptions.FunctionalException;
import io.soffa.foundation.core.exceptions.TechnicalException;
import io.soffa.foundation.core.metrics.CoreMetrics;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import io.soffa.foundation.core.operations.MessageHandler;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.soffa.foundation.core.Constants.OPERATION;
import static io.soffa.foundation.core.metrics.CoreMetrics.*;

public class NatsClient implements BinaryClient {

    private static final Logger LOG = Logger.get(NatsClient.class);
    private final Connection client;
    private final JetStream stream;
    private final String applicationName;
    private final String broadcastSubject;
    private final MetricsRegistry metricsRegistry;

    public NatsClient(String applicationName, String broadcastSubject, String url, MessageHandler handler,
                      MetricsRegistry metricsRegistry) {
        this.broadcastSubject = broadcastSubject;
        this.metricsRegistry = metricsRegistry;
        Options o = new Options.Builder().servers(url.split(",")).maxReconnects(-1).build();
        try {
            this.applicationName = applicationName;
            client = Nats.connect(o);
            JetStreamOptions jso = JetStreamOptions.defaultOptions();
            stream = client.jetStream(jso);

            if (TextUtil.isNotEmpty(broadcastSubject)) {
                StreamConfiguration sc = StreamConfiguration.builder()
                    .name(applicationName)
                    .subjects(broadcastSubject).build();
                JetStreamManagement jsm = client.jetStreamManagement();
                jsm.addStream(sc);
            }
            this.subsribe(applicationName, broadcastSubject, handler);
            LOG.info("Connected to NATS server: %s", url);
        } catch (Exception e) {
            throw new TechnicalException("Unable to connect to NATS @ " + url, e);
        }
    }

    private Map<String, Object> createTags(@Nullable String subject, Message message) {
        Map<String, Object> tags = new HashMap<>();
        if (subject != null) {
            tags.put(OPERATION, message.getOperation());
        }
        tags.put(OPERATION, message.getOperation());
        return tags;
    }

    @Override
    public CompletableFuture<byte[]> request(String subject, Message message) {
        return metricsRegistry.track(
            NATS_REQUEST,
            createTags(subject, message),
            () -> {
                //EL
                return client.request(createNatsMessage(subject, message))
                    .thenApply(io.nats.client.Message::getData);
            });
    }

    @Override
    public <T> CompletableFuture<T> request(String subject, Message message, Class<T> responseClass) {
        return metricsRegistry.track(NATS_REQUEST,
            createTags(subject, message),
            () -> {
                //EL
                return client.request(createNatsMessage(subject, message))
                    .thenApply(msg -> JsonUtil.deserialize(msg.getData(), responseClass));
            });
    }

    @Override
    public void publish(String subject, Message message) {
        metricsRegistry.track(
            NATS_PUBLISH,
            createTags(subject, message),
            () -> client.publish(createNatsMessage(subject, message))
        );
    }

    @Override
    public void broadcast(Message message) {
        //noinspection Convert2Lambda
        metricsRegistry.track(
            NATS_BROADCAST,
            createTags(null, message),
            new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    PublishAck ack = stream.publish(createNatsMessage(broadcastSubject, message));
                    if (ack.hasError()) {
                        throw new TechnicalException(ack.getError());
                    }
                }
            });
    }

    private NatsMessage createNatsMessage(String subject, Message message) {
        return new NatsMessage(subject, "", JsonUtil.serialize(message).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public final void subsribe(String subject, String queue, MessageHandler handler) {

        io.nats.client.MessageHandler localHandler = (msg) -> {
            try {
                Message event = JsonUtil.deserialize(msg.getData(), Message.class);
                Optional<Object> response = handler.onMessage(event);
                if (TextUtil.isNotEmpty(msg.getReplyTo())) {
                    byte[] responseData = response.map(o -> JsonUtil.serialize(o).getBytes(StandardCharsets.UTF_8)).orElse(null);
                    msg.getConnection().publish(msg.getReplyTo(), responseData);
                } else {
                    msg.ack();
                }
                metricsRegistry.increment(CoreMetrics.NATS_EVENT_PROCESSED);
            } catch (FunctionalException e) {
                LOG.warn(TextUtil.format("Message %s was skipped due to a functionnal error", msg.getSID()), e);
                msg.ack();
                metricsRegistry.increment(CoreMetrics.NATS_EVENT_SKIPPED);
            } catch (Exception e) {
                LOG.error("Nats avent handling failed with error", e);
                msg.nak();
                metricsRegistry.increment(CoreMetrics.NATS_EVENT_PROCESSING_FAILED);
            }
        };

        Dispatcher dispatcher = client.createDispatcher(localHandler);
        dispatcher.subscribe(subject);

        if (TextUtil.isNotEmpty(queue)) {
            PushSubscribeOptions so = PushSubscribeOptions.builder()
                .durable(applicationName)
                .build();
            try {
                stream.subscribe(queue, dispatcher, localHandler, false, so);
            } catch (JetStreamApiException | IOException e) {
                throw new TechnicalException(e.getMessage(), e);
            }
        }

    }

    @PreDestroy
    public void cleanup() {
        try {
            client.close();
        } catch (Exception e) {
            LOG.error("Unable to close NATS connection", e);
        }
    }


}
