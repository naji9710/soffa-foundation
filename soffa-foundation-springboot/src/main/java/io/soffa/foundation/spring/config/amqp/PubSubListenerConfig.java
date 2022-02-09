package io.soffa.foundation.spring.config.amqp;

import com.rabbitmq.client.Channel;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.core.actions.MessageHandler;
import io.soffa.foundation.core.actions.MessagesHandler;
import io.soffa.foundation.core.messages.Message;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(value = "app.amqp.enabled", havingValue = "true")
public class PubSubListenerConfig {

    private static final Logger LOG = Logger.get(PubSubListenerConfig.class);
    private final MessageHandler handler;

    public PubSubListenerConfig(MessagesHandler handler) {
        this.handler = handler;
    }

    @SneakyThrows
    @RabbitListener(queues = {"${spring.application.name}"}, ackMode = "MANUAL")
    public void listen(org.springframework.amqp.core.Message message, Channel channel) {
        final long tag = message.getMessageProperties().getDeliveryTag();
        String rawString = new String(message.getBody(), StandardCharsets.UTF_8);
        Message msg;
        try {
            msg = JsonUtil.deserialize(rawString, Message.class);
        } catch (Exception e) {
            LOG.error("[amqp] Invalid Message received", e);
            channel.basicNack(tag, false, false);
            return;
        }
        if (msg == null) {
            LOG.error("[amqp] null event definition received");
            return;
        }
        try {
            if (!handler.accept(msg.getAction())) {
                LOG.error("[amqp] unsupported event %s), skipping.", msg.getAction());
                channel.basicNack(tag, false, false);
                return;
            }
            RequestContextHolder.set(msg.getContext());
            handler.onMessage(msg);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            LOG.error("[amqp] failed to process event %s (%s) -- %s", msg.getAction(), msg.getId());
            channel.basicNack(tag, false, true);
        } finally {
            RequestContextHolder.clear();
        }
    }

}
