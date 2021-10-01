package io.soffa.foundation.spring.config.amqp;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(value = "spring.rabbitmq.addresses", havingValue = "embedded")
public class EmbeddedAMQPConfig {

    @SneakyThrows
    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        RabbitMQConfig.embeddedMode = true;
        return new CachingConnectionFactory(new MockConnectionFactory());
    }

}
