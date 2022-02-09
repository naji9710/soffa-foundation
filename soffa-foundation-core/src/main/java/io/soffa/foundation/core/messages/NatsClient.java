package io.soffa.foundation.core.messages;

import io.nats.client.*;
import io.nats.client.api.PublishAck;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.impl.NatsMessage;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.actions.MessageHandler;
import io.soffa.foundation.exceptions.FunctionalException;
import io.soffa.foundation.exceptions.TechnicalException;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NatsClient implements BinaryClient {

    private static final Logger LOG = Logger.get(NatsClient.class);
    private final Connection client;
    private final JetStream stream;
    private final String applicationName;
    private final String broadcastSubject;
    //private final ExecutorService executor = Executors.newFixedThreadPool(6);

    public NatsClient(String applicationName, String broadcastSubject, String url, MessageHandler handler) {

        this.broadcastSubject = broadcastSubject;
        Options o = new Options.Builder().servers(url.split(",")).maxReconnects(-1).build();
        try {
            this.applicationName = applicationName;
            client = Nats.connect(o);
            JetStreamOptions jso = JetStreamOptions.defaultOptions();
            stream = client.jetStream(jso);

            if (TextUtil.isNotEmpty(broadcastSubject)) {
                StreamConfiguration sc = StreamConfiguration.builder()
                    .name(applicationName)
                    .storageType(StorageType.File)
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

    @Override
    public CompletableFuture<byte[]> request(String subject, Message event) {
        try {
            return client.request(createNatsMessage(subject, event))
                .thenApply(io.nats.client.Message::getData);
        } catch (Exception e) {
            throw new TechnicalException(e.getMessage(), e);
        }
    }

    @Override
    public <T> CompletableFuture<T> request(String subject, Message event, Class<T> responseClass) {
        try {
            return client.request(createNatsMessage(subject, event))
                .thenApply(message -> JsonUtil.deserialize(message.getData(), responseClass));
        } catch (Exception e) {
            throw new TechnicalException(e.getMessage(), e);
        }
    }

    @Override
    public void publish(String subject, Message event) {
        try {
            PublishAck ack = stream.publish(createNatsMessage(subject, event));
            if (ack.hasError()) {
                throw new TechnicalException(ack.getError());
            }
        } catch (JetStreamApiException | IOException e) {
            throw new TechnicalException(e.getMessage(), e);
        }
    }

    @Override
    public void broadcast(Message event) {
        try {
            PublishAck ack = stream.publish(createNatsMessage(broadcastSubject, event));
            if (ack.hasError()) {
                throw new TechnicalException(ack.getError());
            }
        } catch (JetStreamApiException | IOException e) {
            throw new TechnicalException(e.getMessage(), e);
        }
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
            } catch (FunctionalException e) {
                msg.ack();
            } catch (Exception e) {
                LOG.error("Nats avent handling failed with error", e);
                msg.nak();
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
