package io.soffa.foundation.service.config;

import io.soffa.foundation.messages.BinaryClient;
import io.soffa.foundation.messages.MessageHandler;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.service.NatsClient;
import io.soffa.foundation.service.PlatformAuthManager;
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
        PlatformAuthManager authManager,
        MessageHandler messageHandler,
        MetricsRegistry metricsRegistry) {
        return new NatsClient(authManager, applicationName, queue, natsUrl, messageHandler, metricsRegistry);
    }

}
