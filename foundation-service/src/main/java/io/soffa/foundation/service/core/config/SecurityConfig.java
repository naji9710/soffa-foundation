package io.soffa.foundation.service.core.config;

import io.soffa.foundation.core.metrics.MetricsRegistry;
import io.soffa.foundation.core.security.PlatformAuthManager;
import io.soffa.foundation.service.core.RequestFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final PlatformAuthManager authManager;
    private final MetricsRegistry metricsRegistry;
    private final String openApiAccess;

    public SecurityConfig(
        @Value("${app.openapi.access:permitAll}") String openApiAccess,
        MetricsRegistry metricsRegistry, PlatformAuthManager authManager
    ) {
        super();
        this.metricsRegistry = metricsRegistry;
        this.authManager = authManager;
        this.openApiAccess = openApiAccess;
    }

    @Bean
    @ConditionalOnMissingBean(CorsFilter.class)
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            //.and().exceptionHandling()
            //.authenticationEntryPoint((request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
            .and()
            .authorizeRequests()
            .antMatchers("/v3/api-docs").access(openApiAccess)
            .antMatchers("/swagger/*").access(openApiAccess)
            .antMatchers("/actuator/**").permitAll()
            .antMatchers("/**").permitAll()
            .and().addFilterBefore(
                new RequestFilter(authManager, metricsRegistry),
                UsernamePasswordAuthenticationFilter.class
            );
    }

}
