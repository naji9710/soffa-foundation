package io.soffa.foundation.spring;

import io.soffa.foundation.commons.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ConfigLoggerFactory {

    private static final Logger LOG = Logger.get("app");

    private static final class BeansFactoryStateModel {
    }

    @Bean
    public BeansFactoryStateModel beansFactoryState(Environment env) {

        if (LOG.isInfoEnabled()) {
            boolean isAmqpEnabled = env.getProperty("app.amqp.enabled", Boolean.class, false);
            if (isAmqpEnabled) {
                LOG.info("AMQP is enabled with addresses: %s", env.getProperty("app.amqp.addresses"));
            } else {
                LOG.info("AMQP is NOT enabled on this poject");
            }

        }
        return new BeansFactoryStateModel();
    }

}
