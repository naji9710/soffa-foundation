package io.soffa.foundation.spring.config.amqp;

import io.soffa.foundation.core.messages.MessageDispatcher;
import io.soffa.foundation.core.messages.NoopMessageDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBeansFactory {

    @ConditionalOnMissingBean(MessageDispatcher.class)
    public MessageDispatcher createNoopEventDispatcher() {
        return new NoopMessageDispatcher();
    }
}
