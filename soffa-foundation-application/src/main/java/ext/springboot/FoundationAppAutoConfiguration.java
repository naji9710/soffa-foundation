package ext.springboot;

import io.soffa.foundation.core.security.DefaultTokenProvider;
import io.soffa.foundation.core.security.TokenProvider;
import io.soffa.foundation.core.security.model.TokensConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FoundationAppAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "app.security.tokens")
    public TokensConfig createSecurityConfig() {
        return new TokensConfig();
    }

    @Bean
    public TokenProvider createJwtEncoder(TokensConfig config) {
        if (config.isValid()) {
            return new DefaultTokenProvider(config);
        }
        return null;
    }

}
