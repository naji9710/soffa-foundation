package io.soffa.foundation.spring.config;

import io.soffa.foundation.commons.jwt.DefaultJwtProcessor;
import io.soffa.foundation.commons.jwt.JwtEncoderConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBeansFactory {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder createSpringPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public io.soffa.foundation.commons.PasswordEncoder createPasswordEncoder(PasswordEncoder encoder) {
        return new io.soffa.foundation.commons.PasswordEncoder() {

            @Override
            public String encode(String rawPassword) {
                return encoder.encode(rawPassword);
            }

            @Override
            public boolean matches(String rawPassword, String encryptedPassword) {
                return encoder.matches(rawPassword, encryptedPassword);
            }
        };
    }

    @Bean
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${app.security.jwt.secret:}')")
    @ConfigurationProperties(prefix = "app.security.jwt")
    public JwtEncoderConfig createJwtEncoderConfig() {
        return new JwtEncoderConfig();
    }

    @Bean
    @ConditionalOnBean(JwtEncoderConfig.class)
    public DefaultJwtProcessor createJwtEncoder(JwtEncoderConfig config) {
        return new DefaultJwtProcessor(config);
    }

}
