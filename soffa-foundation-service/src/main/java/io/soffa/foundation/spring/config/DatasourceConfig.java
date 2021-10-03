package io.soffa.foundation.spring.config;

import io.soffa.foundation.data.DbConfig;
import io.soffa.foundation.data.MockDataSource;
import io.soffa.foundation.data.TenantAwareDatasource;
import io.soffa.foundation.lang.TextUtil;
import io.soffa.foundation.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
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




}
