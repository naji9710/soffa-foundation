package io.soffa.foundation.pubsub.nats;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.errors.ManagedException;
import io.soffa.foundation.model.Payload;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class NatsMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(NatsMessageHandler.class);

    private final io.soffa.foundation.pubsub.MessageHandler handler;

    @Override
    public void onMessage(Message msg) {
        try {
            io.soffa.foundation.model.Message message = ObjectUtil.deserialize(msg.getData(), io.soffa.foundation.model.Message.class);
            Optional<Object> response = handler.handle(message);
            if (TextUtil.isNotEmpty(msg.getReplyTo())) {
                Object data = response.orElseGet(() -> null);
                msg.getConnection().publish(msg.getReplyTo(), ObjectUtil.serialize(Payload.create(data)));
            } else {
                msg.ack();
            }
        } catch (Exception e) {
            if (e instanceof ManagedException) {
                msg.getConnection().publish(msg.getReplyTo(), ObjectUtil.serialize(Payload.create(e)));
            } else {
                LOG.error("Nats event handling failed with error", e);
                msg.nak();
            }
        }
    }

}
