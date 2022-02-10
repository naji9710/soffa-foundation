package io.soffa.foundation.spring.config;

import io.soffa.foundation.core.actions.MessagesHandler;
import io.soffa.foundation.core.messages.BinaryClient;
import io.soffa.foundation.core.messages.NatsClient;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class NatsBeansFactory {

    @Bean
    @Primary
    public BinaryClient createNatsClient(
        @Value("${spring.application.name}") String applicationName,
        @Value("${app.nats.queue:}") String queue,
        @Value("${app.nats.url}") String natsUrl,
        MessagesHandler messagesHandler,
        MetricsRegistry metricsRegistry) {
        return new NatsClient(applicationName, queue, natsUrl, messagesHandler, metricsRegistry);
    }

}
