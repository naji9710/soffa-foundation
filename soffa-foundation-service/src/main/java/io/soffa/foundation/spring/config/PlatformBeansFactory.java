package io.soffa.foundation.spring.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.soffa.foundation.commons.ErrorUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.application.AppConfig;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import io.soffa.foundation.core.operations.Operation;
import io.soffa.foundation.core.operations.Operation0;
import io.soffa.foundation.core.web.OpenApiBuilder;
import io.soffa.foundation.spring.OperationsMapping;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

@Configuration
public class PlatformBeansFactory {

    @Bean
    public RestTemplate createDefaultRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OperationsMapping createOperationsMapping(Set<Operation<?, ?>> operationHandlers,
                                                  Set<Operation0<?>> operationHandlers0) {
        return new OperationsMapping(operationHandlers, operationHandlers0);
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults(@Value("${app.package}") String defaultPackage,
                                                             @Value("${spring.application.name}") String serviceId) {
        if (TextUtil.isEmpty(defaultPackage)) {
            Logger.setRelevantPackage(defaultPackage);
            ErrorUtil.setRelevantPackage(defaultPackage);
        }
        RequestContext.setServiceName(serviceId);
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "app")
    public AppConfig createAppConfig() {
        return new AppConfig();
    }


    @Bean
    @Primary
    public HttpFirewall looseHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setUnsafeAllowAnyHttpMethod(true);
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }


    @Bean
    public OpenAPI createOpenAPI(AppConfig appConfig) {
        OpenApiBuilder builder = new OpenApiBuilder(appConfig.getOpenapi());
        return builder.build();
    }

    @Bean
    @Primary
    public MetricsRegistry createMetricsRegistry(MeterRegistry registry) {
        return new MetricsRegistryImpl(registry);
    }


}
