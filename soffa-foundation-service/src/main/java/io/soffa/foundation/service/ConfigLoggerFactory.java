package io.soffa.foundation.service;

import io.soffa.foundation.commons.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ConfigLoggerFactory {

    private static final Logger LOG = Logger.get("app", "app.resources");

    private static final class BeansFactoryStateModel {
    }

    @Bean
    public BeansFactoryStateModel beansFactoryState(Environment env) {

        if (LOG.isInfoEnabled()) {
            boolean isAmqpEnabled = env.getProperty("app.amqp.enabled", Boolean.class, false);
            if (isAmqpEnabled) {
                LOG.info("AMQP is enabled with addresses: %s", env.getProperty("app.amqp.addresses"));
            } else {
                LOG.info("AMQP is *NOT* enabled on this poject");
            }

            boolean isConsulEnabled = env.getProperty("spring.cloud.consul.enabled", Boolean.class, false);
            if (isConsulEnabled) {
                LOG.info("Consul discovery is enabled with server: %s:%s", env.getProperty("spring.cloud.consul.host"), env.getProperty("spring.cloud.consul.port"));
            } else {
                LOG.info("Consul discovery is *NOT* enabled on this poject");
            }

        }
        return new BeansFactoryStateModel();
    }

}
