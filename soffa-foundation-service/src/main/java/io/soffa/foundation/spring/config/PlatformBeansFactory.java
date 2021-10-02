package io.soffa.foundation.spring.config;

import io.soffa.foundation.actions.ActionDispatcher;
import io.soffa.foundation.actions.DefaultActionDispatcher;
import io.soffa.foundation.core.Action;
import io.soffa.foundation.core.Action0;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.data.DbConfig;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.exceptions.ErrorUtil;
import io.soffa.foundation.lang.TextUtil;
import io.soffa.foundation.logging.Logger;
import io.soffa.foundation.web.OpenAPIDesc;
import io.soffa.foundation.web.OpenApiBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.util.Set;

@Configuration
public class PlatformBeansFactory {

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnMissingBean(ActionDispatcher.class)
    public ActionDispatcher createActionDispatcher(Set<Action<?, ?>> actionHandlers,
                                                   Set<Action0<?>> action0Handlers,
                                                   @Autowired(required = false) SysLogRepository syLogs) {
        return new DefaultActionDispatcher(actionHandlers, action0Handlers, syLogs);
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
    @ConfigurationProperties(prefix = "app.db")
    @ConditionalOnMissingBean(DbConfig.class)
    public DbConfig createDbConfig() {
        return new DbConfig();
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
    @ConfigurationProperties(prefix = "app.openapi")
    public OpenAPIDesc createOpenAPIDesc() {
        return new OpenAPIDesc();
    }


    @Bean
    public OpenAPI createOpenAPI(OpenAPIDesc desc) {
        OpenApiBuilder builder = new OpenApiBuilder(desc);
        return builder.build();
    }

}
