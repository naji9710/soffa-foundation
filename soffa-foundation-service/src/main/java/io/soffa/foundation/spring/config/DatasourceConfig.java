package io.soffa.foundation.spring.config;

import io.soffa.foundation.commons.lang.TextUtil;
import io.soffa.foundation.commons.logging.Logger;
import io.soffa.foundation.service.data.DbConfig;
import io.soffa.foundation.service.data.TenantAwareDatasource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(DbConfig.class)
public class DatasourceConfig {

    private static final Logger LOG = Logger.create(DatasourceConfig.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public TenantAwareDatasource createDatasource(
            DbConfig dbConfig,
            @Value("${spring.application.name}") String applicationName) {

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
