package io.soffa.foundation.pubsub.nats;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.errors.ManagedException;
import io.soffa.foundation.model.OperationResult;
import kotlin.Unit;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class NatsMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(NatsMessageHandler.class);

    private final Connection connection;
    private final io.soffa.foundation.pubsub.MessageHandler handler;

    @Override
    public void onMessage(Message msg) {
        boolean hasReply = TextUtil.isNotEmpty(msg.getReplyTo());
        try {
            byte[] data = msg.getData();
            if (data==null) {
                return;
            }
            io.soffa.foundation.model.Message message = ObjectUtil.deserialize(data, io.soffa.foundation.model.Message.class);
            if (message==null) {
                return;
            }
            Optional<Object> operationResult = handler.handle(message);
            if (operationResult.isPresent() && hasReply) {
                Object result = operationResult.get();
                Class<?> className = result.getClass();
                boolean isNoop = className == Unit.class || className == Void.class;
                if (!isNoop) {
                    OperationResult response = OperationResult.create(operationResult.orElse(null), null);
                    LOG.debug("Sending response back to %s [SID:%s]", msg.getReplyTo(), msg.getSID());
                    connection.publish(msg.getReplyTo(), msg.getSubject(), ObjectUtil.serialize(response));
                }
            }
        } catch (Exception e) {
            LOG.error("Nats event handling failed with error", e);
            if (e instanceof ManagedException) {
                if (hasReply) {
                    connection.publish(msg.getReplyTo(), msg.getSubject(), ObjectUtil.serialize(OperationResult.create(null, e)));
                }
            } else {
                throw e;
            }
        }
    }

}
