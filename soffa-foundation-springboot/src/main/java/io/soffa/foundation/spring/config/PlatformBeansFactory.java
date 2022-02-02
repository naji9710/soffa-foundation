package io.soffa.foundation.spring.config;

import io.soffa.foundation.actions.Action;
import io.soffa.foundation.actions.Action0;
import io.soffa.foundation.actions.ActionDispatcher;
import io.soffa.foundation.actions.EventHandler;
import io.soffa.foundation.commons.ErrorUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.config.AppConfig;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.metrics.NoopMetricsRegistryImpl;
import io.soffa.foundation.spring.DefaultActionDispatcher;
import io.soffa.foundation.web.OpenApiBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    @ConditionalOnMissingBean(ActionDispatcher.class)
    public ActionDispatcher createActionDispatcher(Set<Action<?, ?>> actionHandlers,
                                                   Set<Action0<?>> action0Handlers) {
        return new DefaultActionDispatcher(actionHandlers, action0Handlers);
    }

    @Bean
    @ConditionalOnMissingBean(ActionDispatcher.class)
    public EventHandler createDefaultEventsHandler(Set<Action<?, ?>> actionHandlers,
                                                   Set<Action0<?>> action0Handlers) {
        return new DefaultActionDispatcher(actionHandlers, action0Handlers);
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
    @ConditionalOnMissingBean(MetricsRegistry.class)
    public MetricsRegistry createDefaultMetricsRegistry() {
        return new NoopMetricsRegistryImpl();
    }

}
