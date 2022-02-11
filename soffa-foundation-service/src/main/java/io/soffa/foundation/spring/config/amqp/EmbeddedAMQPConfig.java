package io.soffa.foundation.spring.config.amqp;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import io.soffa.foundation.commons.Logger;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(value = "app.amqp.enabled", havingValue = "true")
public class EmbeddedAMQPConfig {

    private static final Logger LOG = Logger.get(EmbeddedAMQPConfig.class);

    @SneakyThrows
    @Bean
    @Primary
    @ConditionalOnProperty(value = "spring.rabbitmq.addresses", havingValue = "embedded")
    public ConnectionFactory connectionFactory() {
        LOG.info("Embedded AMQP server enabled");
        RabbitMQConfig.embeddedMode = true;
        return new CachingConnectionFactory(new MockConnectionFactory());
    }

}
