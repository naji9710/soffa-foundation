package io.soffa.foundation.spring.config.amqp;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.messages.MessageDispatcher;
import io.soffa.foundation.core.messages.NoopMessageDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBeansFactory {

    private static final Logger LOG = Logger.get(EventBeansFactory.class);

    @Bean
    @ConditionalOnMissingBean(MessageDispatcher.class)
    public MessageDispatcher createNoopEventDispatcher() {
        LOG.warn("No message dispatcher configured. Using NoopMessageDispatcher");
        return new NoopMessageDispatcher();
    }
}
