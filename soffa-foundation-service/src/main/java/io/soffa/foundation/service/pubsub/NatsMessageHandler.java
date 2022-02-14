package io.soffa.foundation.service.pubsub;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.exceptions.ManagedException;
import io.soffa.foundation.metrics.CoreMetrics;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.service.PlatformAuthManager;
import lombok.AllArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@AllArgsConstructor
public class NatsMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(NatsMessageHandler.class);

    private final MetricsRegistry metricsRegistry;
    private final PlatformAuthManager authManager;
    private final io.soffa.foundation.messages.MessageHandler handler;
    private final boolean sendAck;

    @Override
    public void onMessage(Message msg) throws InterruptedException {
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
            } else if (sendAck) {
                msg.ack();
            }
            metricsRegistry.increment(CoreMetrics.NATS_EVENT_PROCESSED);
        } catch (Exception e) {
            LOG.error("Nats avent handling failed with error", e);
            if (!(e instanceof ManagedException) && sendAck) {
                msg.nak();
            }
            metricsRegistry.increment(CoreMetrics.NATS_EVENT_PROCESSING_FAILED);
            if (!sendAck) {
                throw e;
            }
        }
    }
}
