package io.soffa.foundation.spring.config;

import io.soffa.foundation.actions.EventHandler;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.events.Event;
import io.soffa.foundation.pubsub.BinaryClient;
import io.soffa.foundation.pubsub.BinaryMessageHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class BinaryReceiver implements BinaryMessageHandler {

    private final EventHandler eventHandler;

    public BinaryReceiver(
        EventHandler eventHandler,
        BinaryClient client,
        @Value("${app.nats.binding:}") String natsbinding,
        @Value("${spring.application.name}") String applicationName) {
        //this.client = client;
        this.eventHandler = eventHandler;
        String binding = natsbinding;
        if (TextUtil.isEmpty(binding)) {
            binding = applicationName;
        }
        client.subsribe(binding, this);
    }



    @Override
    public Optional<Object> onMessage(Event msg) {
        return eventHandler.handle(msg);
    }
}
