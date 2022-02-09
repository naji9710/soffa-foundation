package io.soffa.foundation.spring.config;

import io.soffa.foundation.core.actions.MessageHandler;
import io.soffa.foundation.core.messages.BinaryClient;
import io.soffa.foundation.core.messages.NatsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class NatsBeansFactory {

    @Bean
    public BinaryClient createNatsClient(
        MessageHandler messageHandler,
        @Value("${spring.application.name}") String applicationName,
        @Value("${app.nats.queue:}") String queue,
        @Value("${app.nats.url}") String natsUrl) {
        return new NatsClient(messageHandler, applicationName, queue, natsUrl);
    }

}
