package io.soffa.foundation.service.core.config;

import io.soffa.foundation.core.AppConfig;
import io.soffa.foundation.core.TenantsLoader;
import io.soffa.foundation.core.pubsub.PubSubMessenger;
import io.soffa.foundation.core.security.TokenProvider;
import io.soffa.foundation.core.service.PubSubTenantsLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "app.tenants-provider.pubsub.enabled", havingValue = "true")
public class PubSubTenantsLoaderConfig {

    @Bean
    public TenantsLoader createPubSubTenantsLoader(PubSubMessenger client,
                                                   TokenProvider tokens,
                                                   AppConfig app,
                                                   @Value("${app.tenants-provider.pubsub.subject}") String serviceId,
                                                   @Value("${app.tenants-provider.pubsub.token-permission:service}") String permission) {
        return new PubSubTenantsLoader(client, tokens, app, serviceId, permission);
    }
}
