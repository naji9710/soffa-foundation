package io.soffa.foundation.service.config;

import io.soffa.foundation.config.AppConfig;
import io.soffa.foundation.messages.MessageHandler;
import io.soffa.foundation.messages.PubSubClient;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.service.PlatformAuthManager;
import io.soffa.foundation.service.pubsub.NatsClient;
import io.soffa.foundation.service.pubsub.nats.NatsConfig;
import io.soffa.foundation.service.state.DatabasePlane;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class NatsBeansFactory {

    @Bean
    @ConfigurationProperties(prefix = "app.nats")
    public NatsConfig createNatsConfig() {
        return new NatsConfig();
    }

    @Bean
    @Primary
    public PubSubClient createNatsClient(
        AppConfig appConfig,
        PlatformAuthManager authManager,
        MessageHandler messageHandler,
        NatsConfig natsConfig,
        DatabasePlane dbPlane,
        MetricsRegistry metricsRegistry) {
        return new NatsClient(
            authManager,
            appConfig.getName(),
            natsConfig,
            messageHandler,
            metricsRegistry,
            dbPlane
        );
    }

}
