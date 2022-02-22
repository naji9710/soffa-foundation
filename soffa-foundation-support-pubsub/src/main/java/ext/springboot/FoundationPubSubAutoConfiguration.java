package ext.springboot;

import io.soffa.foundation.core.pubsub.MessageHandler;
import io.soffa.foundation.core.pubsub.PubSubConfig;
import io.soffa.foundation.core.pubsub.PubSubMessenger;
import io.soffa.foundation.service.pubsub.PubSubMessengerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "app.pubsub.enabled", havingValue = "true", matchIfMissing = true)
public class FoundationPubSubAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "app.pubsub")
    public PubSubConfig createNatsConfig() {
        return new PubSubConfig();
    }

    @Bean
    @Primary
    public PubSubMessenger createPubSubMessenger(@Value("${spring.application.name}") String applicationName,
                                                 PubSubConfig config,
                                                 @Autowired(required = false) MessageHandler handler) {
        PubSubMessenger messenger = PubSubMessengerFactory.create(applicationName, config, handler);
        messenger.afterPropertiesSet();
        return messenger;
    }

}
