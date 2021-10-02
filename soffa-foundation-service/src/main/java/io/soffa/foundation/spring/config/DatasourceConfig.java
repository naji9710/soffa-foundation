package io.soffa.foundation.spring.config;

import io.soffa.foundation.actions.ActionDispatcher;
import io.soffa.foundation.data.DbConfig;
import io.soffa.foundation.data.MockDataSource;
import io.soffa.foundation.data.TenantAwareDatasource;
import io.soffa.foundation.lang.TextUtil;
import io.soffa.foundation.logging.Logger;
import io.soffa.foundation.spring.config.jobs.JobManager;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.jobs.filters.RetryFilter;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

    private static final Logger LOG = Logger.create(DatasourceConfig.class);

    @Bean
    @Primary
    public DataSource createDatasource(
            DbConfig dbConfig,
            @Value("${spring.application.name}") String applicationName) {

        if (dbConfig.getLinks()==null || dbConfig.getLinks().isEmpty()) {
            return new MockDataSource();
        }

        String tablePrefix = TextUtil.trimToEmpty(dbConfig.getTablePrefix()).replaceAll("[^a-zA-Z0-9]", "_");
        TenantAwareDatasource ds = new TenantAwareDatasource(dbConfig.getLinks(), tablePrefix, applicationName);
        if (dbConfig.isAutoMigrate()) {
            ds.applyMigrations();
        }else {
            LOG.warn("Automatic database migration is disable for this instance (app.db.auto-migrate=false)");
        }
        return ds;
    }

    @Bean
    @ConditionalOnProperty(value = "app.sys-jobs.enabled", havingValue = "true")
    public JobManager createJobManager(DataSource ds, ActionDispatcher actionDispatcher, ApplicationContext applicationContext) {
        DataSource target = ds;
        if (ds instanceof TenantAwareDatasource) {
            target = ((TenantAwareDatasource)ds).getDefault();
        }
        JobRunrConfiguration.JobRunrConfigurationResult config = JobRunr
            .configure()
            .withJobFilter(new RetryFilter(10))
            .useStorageProvider(SqlStorageProviderFactory.using(target))
            .useJobActivator(applicationContext::getBean)
            .useBackgroundJobServer()
            .useJmxExtensions()
            .useDashboard()
            .initialize();
        return new JobManager(actionDispatcher, config);
    }


}
