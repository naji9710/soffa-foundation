package io.soffa.foundation.spring.config;

import io.soffa.foundation.core.actions.MessagesHandler;
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
        @Value("${spring.application.name}") String applicationName,
        @Value("${app.nats.queue:}") String queue,
        @Value("${app.nats.url}") String natsUrl,
        MessagesHandler messagesHandler) {
        return new NatsClient(applicationName, queue, natsUrl, messagesHandler);
    }

}
