package io.soffa.spring.config;

import io.soffa.service.data.DbConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestBeansFactory {


    @Bean
    @Primary
    @ConfigurationProperties(prefix = "app.db")
    public DbConfig createDbConfig() {
        return new DbConfig();
    }

}
