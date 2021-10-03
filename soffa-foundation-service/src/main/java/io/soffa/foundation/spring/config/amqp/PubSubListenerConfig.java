package io.soffa.foundation.spring.config.amqp;

import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.events.Event;
import io.soffa.foundation.logging.Logger;
import io.soffa.foundation.pubsub.PubSubListener;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class PubSubListenerConfig {

    private static final Logger LOG = Logger.create(PubSubListenerConfig.class);
    private final PubSubListener listener;

    public PubSubListenerConfig(@Autowired(required = false) PubSubListener listener) {
        this.listener = listener;
    }

    @SneakyThrows
    @RabbitListener(queues = {"${spring.application.name}"}, ackMode="AUTO")
    public void listen(Message message) {
        if (listener == null) {
            LOG.warn("No event listener registered");
            return;
        }
        String rawString = new String(message.getBody(), StandardCharsets.UTF_8);
        Event event;
        try {
            event = JsonUtil.deserialize(rawString, Event.class);
        } catch (Exception e) {
            LOG.error("[amqp] Invalid Event received", e);
            return;
        }
        if (event == null) {
            LOG.error("[amqp] null event definition received");
            return;
        }
        RequestContextHolder.set(event.getContext());
        TenantHolder.use(event.getTenantId(), () -> {
            listener.handle(event);
        });
    }

}
