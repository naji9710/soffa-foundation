package io.soffa.foundation.spring.config.amqp;

import com.rabbitmq.client.Channel;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.events.Event;
import io.soffa.foundation.pubsub.PubSubListener;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(value = "app.amqp.enabled", havingValue = "true")
public class PubSubListenerConfig {

    private static final Logger LOG = Logger.get(PubSubListenerConfig.class);
    private final PubSubListener listener;

    public PubSubListenerConfig(@Autowired(required = false) PubSubListener listener) {
        this.listener = listener;
    }

    @SneakyThrows
    @RabbitListener(queues = {"${spring.application.name}"}, ackMode = "MANUAL")
    public void listen(Message message, Channel channel) {
        final long tag = message.getMessageProperties().getDeliveryTag();
        if (listener == null) {
            LOG.warn("No event listener registered");
            channel.basicNack(tag, false, false);
            return;
        }
        String rawString = new String(message.getBody(), StandardCharsets.UTF_8);
        Event event;
        try {
            event = JsonUtil.deserialize(rawString, Event.class);
        } catch (Exception e) {
            LOG.error("[amqp] Invalid Event received", e);
            channel.basicNack(tag, false, false);
            return;
        }
        if (event == null) {
            LOG.error("[amqp] null event definition received");
            return;
        }
        try {

            if (!listener.accept(event.getAction())){
                LOG.error("[amqp] unsupported event %s), skipping.", event.getAction());
                return;
            }

            RequestContextHolder.set(event.getContext());
            listener.handle(event);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            LOG.error("[amqp] failed to process event %s (%s) -- %s", event.getAction(), event.getId());
            channel.basicNack(tag, false, true);
        } finally {
            RequestContextHolder.clear();
        }
    }

}
