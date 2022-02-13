package io.soffa.foundation.service.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.config.DataSourceConfig;
import io.soffa.foundation.data.DataSourceProperties;
import io.soffa.foundation.exceptions.DatabaseException;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.model.TenantId;
import liquibase.integration.spring.SpringLiquibase;
import lombok.SneakyThrows;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public final class DbHelper {

    private static final Logger LOG = Logger.get(DbHelper.class);
    private static final ResourceLoader RL = new DefaultResourceLoader();

    private DbHelper() {
    }

    @SneakyThrows
    public static HikariDataSource createDataSource(DataSourceProperties config, DataSourceConfig link) {

        HikariConfig hc = new HikariConfig();

        hc.setDriverClassName(config.getDriverClassName());
        hc.setUsername(config.getUsername());
        hc.setPassword(config.getPassword());
        hc.setJdbcUrl(config.getUrl());
        hc.setPoolName(IdGenerator.shortUUID(config.getName() + "_"));
        hc.setConnectionTestQuery("select 1");

        hc.setMinimumIdle(10_000);
        hc.setConnectionTimeout(30_000);
        hc.setIdleTimeout(35_000);
        hc.setMaxLifetime(45_000);
        hc.setMaximumPoolSize(20);
        hc.setMaxLifetime(1000);

        if (config.getUrl().contains(":h2:")) {
            hc.addDataSourceProperty("ignore_startup_parameters", "search_path");
        }

        if (config.hasSchema()) {
            hc.setSchema(config.getSchema());
        }
        link.setName(config.getName());
        return new HikariDataSource(hc);
    }


    public static void applyMigrations(DataSource dataSource, String changeLogPath, String tablesPrefix, String appicationName) {
        SpringLiquibase lqb = new SpringLiquibase();
        lqb.setDropFirst(false);
        lqb.setResourceLoader(RL);
        Map<String, String> changeLogParams = new HashMap<>();

        changeLogParams.put("table_prefix", "");
        changeLogParams.put("tables_prefix", "");
        changeLogParams.put("tablePrefix", "");
        changeLogParams.put("tablesPrefix", "");

        if (TextUtil.isNotEmpty(tablesPrefix)) {
            changeLogParams.put("table_prefix", tablesPrefix);
            changeLogParams.put("tables_prefix", tablesPrefix);
            changeLogParams.put("tablePrefix", tablesPrefix);
            changeLogParams.put("tablesPrefix", tablesPrefix);

            lqb.setDatabaseChangeLogLockTable(tablesPrefix + "changelog_lock");
            lqb.setDatabaseChangeLogTable(tablesPrefix + "changelog");
        }
        if (TextUtil.isNotEmpty(appicationName)) {
            changeLogParams.put("application", appicationName);
            changeLogParams.put("applicationName", appicationName);
            changeLogParams.put("application_name", appicationName);
        }

        Resource res = RL.getResource(changeLogPath);
        if (!res.exists()) {
            throw new TechnicalException("Liquibase changeLog was not found: %s", changeLogPath);
        }
        lqb.setChangeLog(changeLogPath);
        doApplyMigration(lqb, changeLogParams, (HikariDataSource) dataSource);
    }

    private static void doApplyMigration(SpringLiquibase lqb, Map<String, String> changeLogParams, final HikariDataSource ds) {
        String schema = ds.getSchema();
        String dsName = ds.getPoolName().split("__")[0];
        if (TenantId.DEFAULT_VALUE.equals(dsName)) {
            lqb.setContexts(TenantId.DEFAULT_VALUE);
        } else {
            lqb.setContexts("tenant," + dsName.toLowerCase());
        }
        if (TextUtil.isNotEmpty(schema)) {
            lqb.setDefaultSchema(schema);
            lqb.setLiquibaseSchema(schema);
        }
        lqb.setChangeLogParameters(changeLogParams);
        try {
            lqb.setDataSource(ds);

            lqb.afterPropertiesSet(); // Run migrations
            LOG.info("[datasource:%s] migration '%s' successfully applied", dsName, lqb.getChangeLog());
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("changelog") && msg.contains("already exists")) {
                boolean isTestDb = ((HikariDataSource)lqb.getDataSource()).getJdbcUrl().startsWith("jdbc:h2:mem");
                if (!isTestDb) {
                    LOG.warn("Looks like migrations are being ran twice for %s.%s, ignore this error", dsName, schema);
                }
            }else {
                throw new DatabaseException(e, "Migration failed for %s", schema);
            }
        }
    }

}
