package io.soffa.foundation.pubsub;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.impl.NatsMessage;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.events.Event;
import io.soffa.foundation.exceptions.ManagedException;
import io.soffa.foundation.exceptions.TechnicalException;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NatsClient implements BinaryClient {

    private static final Logger LOG = Logger.get(NatsClient.class);
    private final Connection client;
    //private final ExecutorService executor = Executors.newFixedThreadPool(6);

    public NatsClient(String url) {
        Options o = new Options.Builder().servers(url.split(",")).maxReconnects(-1).build();
        try {
            client = Nats.connect(o);
        } catch (IOException | InterruptedException e) {
            throw new TechnicalException("Unable to connect to NATS", e);
        }
    }

    @Override
    public CompletableFuture<byte[]> request(String subject, Event event) {
        try {
            return client.request(createNatsMessage(subject, event, true))
                .thenApply(Message::getData);
        } catch (Exception e) {
            throw new TechnicalException(e.getMessage(), e);
        }
    }
    @Override
    public <T> CompletableFuture<T> request(String subject, Event event, Class<T> responseClass) {
        try {
            return client.request(createNatsMessage(subject, event, true))
                .thenApply(message -> {
                    return JsonUtil.deserialize(message.getData(), responseClass);
                });
        } catch (Exception e) {
            throw new TechnicalException(e.getMessage(), e);
        }
    }

    @Override
    public void publish(String subject, Event event) {
        client.publish(createNatsMessage(subject, event, false));
    }

    private NatsMessage createNatsMessage(String subject, Event event, boolean request) {
        final String replyTo = request ? IdGenerator.secureRandomId() : "";
        return new NatsMessage(subject, replyTo, JsonUtil.serialize(event).getBytes(StandardCharsets.UTF_8));
    }

    public void subsribe(String subject, BinaryMessageHandler handler) {
        client.createDispatcher(msg -> {
            try {
                Event event = JsonUtil.deserialize(msg.getData(), Event.class);
                Optional<Object> response = handler.onMessage(event);
                if (TextUtil.isNotEmpty(msg.getReplyTo())) {
                    byte[] responseData = response.map(o -> JsonUtil.serialize(o).getBytes(StandardCharsets.UTF_8)).orElse(null);
                    msg.getConnection().publish(msg.getReplyTo(), responseData);
                } else {
                    msg.ack();
                }
            } catch (Exception e) {
                LOG.error("Nats avent handling failed with error", e);
                if (e instanceof ManagedException) {
                    msg.ack();
                } else {
                    msg.nak();
                }
            }
        }).subscribe(subject);
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
