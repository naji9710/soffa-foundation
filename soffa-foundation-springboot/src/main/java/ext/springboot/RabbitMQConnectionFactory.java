package ext.springboot;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.spring.config.amqp.model.RabbitMQProperties;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@Configuration
@ConditionalOnProperty(value = "app.amqp.enabled", havingValue = "true")
@Import(RabbitAutoConfiguration.class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class RabbitMQConnectionFactory {

    private static final Logger LOG = Logger.get(RabbitMQConnectionFactory.class);

    @Bean
    @ConfigurationProperties(prefix = "app.amqp")
    public RabbitMQProperties properties() {
        return new RabbitMQProperties();
    }

}
