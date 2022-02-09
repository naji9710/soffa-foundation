package io.soffa.foundation.spring.config;

import io.soffa.foundation.pubsub.BinaryClient;
import io.soffa.foundation.pubsub.NatsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class NatsBeansFactory {

    @Bean
    public BinaryClient createNatsClient(@Value("${app.nats.url}") String natsUrl) {
        return new NatsClient(natsUrl);
    }

}
