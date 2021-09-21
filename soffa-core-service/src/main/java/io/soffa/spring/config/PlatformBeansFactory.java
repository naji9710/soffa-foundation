package io.soffa.spring.config;

import io.soffa.commons.exceptions.Errors;
import io.soffa.commons.lang.TextUtil;
import io.soffa.commons.logging.Logger;
import io.soffa.service.actions.ActionDispatcher;
import io.soffa.service.actions.DefaultActionDispatcher;
import io.soffa.service.core.Action;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    public ActionDispatcher createActionDispatcher(Set<Action<?, ?>> actionHandlers, @Value("${app.package:}") String defaultPackage) {
        if (TextUtil.isEmpty(defaultPackage)) {
            Logger.setRelevantPackage(defaultPackage);
            Errors.setRelevantPackage(defaultPackage);
        }
        return new DefaultActionDispatcher(actionHandlers);
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    @Primary
    public HttpFirewall looseHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setUnsafeAllowAnyHttpMethod(true);
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }

}
