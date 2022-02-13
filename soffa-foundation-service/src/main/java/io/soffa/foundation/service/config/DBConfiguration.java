package io.soffa.foundation.service.config;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.config.AppConfig;
import io.soffa.foundation.data.TenantsLoader;
import io.soffa.foundation.service.data.MockDataSource;
import io.soffa.foundation.service.data.DBImpl;
import io.soffa.foundation.service.state.DatabasePlane;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DBConfiguration {

    private static final Logger LOG = Logger.get(DBConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public TenantsLoader createDefaultTenantsLoader() {
        return TenantsLoader.NOOP;
    }

    @Bean
    @Primary
    public DataSource createDatasource(AppConfig appConfig,
                                       TenantsLoader tenantsLoader,
                                       DatabasePlane dbState,
                                       ApplicationEventPublisher publisher,
                                       @Value("${spring.application.name}") String applicationName) {
        if (!appConfig.hasDataSources()) {
            LOG.info("No datasources configured for this service.");
            dbState.setReady();
            return new MockDataSource();
        }
        return new DBImpl(tenantsLoader, dbState, appConfig.getDb(), applicationName, publisher);
    }


}
