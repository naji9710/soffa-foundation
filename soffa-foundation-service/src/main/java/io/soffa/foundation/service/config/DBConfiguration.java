package io.soffa.foundation.service.config;

import io.soffa.foundation.config.AppConfig;
import io.soffa.foundation.data.DB;
import io.soffa.foundation.data.TenantsLoader;
import io.soffa.foundation.messages.BinaryClient;
import io.soffa.foundation.service.data.DBImpl;
import io.soffa.foundation.service.state.DatabasePlane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DBConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TenantsLoader createDefaultTenantsLoader() {
        return TenantsLoader.NOOP;
    }

    @Bean
    public DB createDB(AppConfig appConfig,
                               TenantsLoader tenantsLoader,
                               DatabasePlane dbState,
                               ApplicationEventPublisher publisher,
                               @Autowired(required = false) BinaryClient binaryClient,
                               @Value("${spring.application.name}") String applicationName) {

        appConfig.configure();
        return new DBImpl(tenantsLoader, dbState, binaryClient, appConfig.getDb(), applicationName, publisher);
    }

    @Bean
    @Primary
    public DataSource createDatasource(DB db) {
        return (DataSource)db;
    }


}
