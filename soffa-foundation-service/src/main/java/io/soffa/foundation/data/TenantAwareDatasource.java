package io.soffa.foundation.data;

import com.zaxxer.hikari.HikariDataSource;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.exceptions.DatabaseException;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.lang.TextUtil;
import io.soffa.foundation.logging.Logger;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TenantAwareDatasource extends AbstractRoutingDataSource implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = Logger.create(TenantAwareDatasource.class);
    private final Map<Object, Object> dataSources = new ConcurrentHashMap<>();
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final String tablesPrefix;
    private final String appicationName;
    private boolean appicationStarted;
    public static final String NONE = "NONE";
    public static final String DEFAULT = "default";

    @Override
    protected Object determineCurrentLookupKey() {
        if (TenantHolder.isEmpty()) {
            if (dataSources.containsKey(DEFAULT)) {
                return DEFAULT;
            } else if (!appicationStarted) {
                return NONE;
            }
        }
        String linkId = TenantHolder.get().orElseThrow(() -> new DatabaseException("Missing database link. Don't forget to set active tenant with TenantHolder.set()"));
        if (!dataSources.containsKey(linkId)) {
            throw new DatabaseException("{0} is not a valid database link", linkId);
        }
        return linkId;
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        appicationStarted = true;
    }

    @SneakyThrows
    public TenantAwareDatasource(final Map<String, String> links,
                                 final String tablesPrefix,
                                 final String appicationName) {

        super();
        this.tablesPrefix = tablesPrefix;
        this.appicationName = appicationName;
        setLenientFallback(false);
        CustomPhysicalNamingStrategy.tablePrefix = tablesPrefix;
        if (links == null || links.isEmpty()) {
            throw new TechnicalException("No db link provided");
        }
        links.forEach((name, url) -> {
            dataSources.put(name, createDataSource(DataSourceProperties.create(name, url.trim())));
        });
        dataSources.put(NONE, new MockDataSource());
        super.setTargetDataSources(dataSources);
    }


    @SneakyThrows
    private DataSource createDataSource(DataSourceProperties config) {

        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder
            .create().driverClassName(config.getDriverClassName())
            .username(config.getUsername())
            .password(config.getPassword())
            .url(config.getUrl())
            .build();

        dataSource.setConnectionTestQuery("select 1");
        dataSource.setIdleTimeout(60_000);
        dataSource.setMaximumPoolSize(30);
        dataSource.setMinimumIdle(3);
        dataSource.setValidationTimeout(10_000);
        dataSource.setPoolName(appicationName + "_" + config.getName() + "__" + RandomUtils.nextInt());

        if (config.hasSchema()) {
            dataSource.setSchema(config.getSchema());
        }
        return dataSource;
    }

    public void applyMigrations() {
        String changeLog = "/db/changelog/" + appicationName + ".xml";
        for (Object value : dataSources.values()) {
            if (value instanceof HikariDataSource) {
                applyMigrations((DataSource) value, changeLog);
            }
        }
    }

    /*
    public void applyMigrations(String tenant) {
        applyMigrations(get(tenant), "/db/changelog/" + appicationName + ".xml");
    }

    public void applyMigrations(String tenant, String changeLogPath) {
        applyMigrations(get(tenant), changeLogPath);
    }

    */

    public void applyMigrations(DataSource dataSource, String changeLogPath) {
        Resource res = resourceLoader.getResource(changeLogPath);
        if (!res.exists()) {
            throw new TechnicalException("Liquibase changeLog was not found: {0}", changeLogPath);
        }
        SpringLiquibase lqb = new SpringLiquibase();
        lqb.setChangeLog(changeLogPath);
        lqb.setDropFirst(false);
        lqb.setDataSource(dataSource);
        lqb.setResourceLoader(resourceLoader);
        Map<String, String> changeLogParams = new HashMap<>();

        changeLogParams.put("table_prefix", "");
        changeLogParams.put("tablePrefix", "");

        if (TextUtil.isNotEmpty(tablesPrefix)) {
            changeLogParams.put("table_prefix", tablesPrefix + "_");
            changeLogParams.put("tablePrefix", tablesPrefix + "_");

            lqb.setDatabaseChangeLogLockTable(tablesPrefix + "_changelog_lock");
            lqb.setDatabaseChangeLogTable(tablesPrefix + "_changelog");
        }
        if (TextUtil.isNotEmpty(appicationName)) {
            changeLogParams.put("application", appicationName);
            changeLogParams.put("applicationName", appicationName);
            changeLogParams.put("application_name", appicationName);
        }

        doApplyMigration(lqb, changeLogParams, (HikariDataSource) dataSource);
    }

    private void doApplyMigration(SpringLiquibase lqb, Map<String, String> changeLogParams, HikariDataSource ds) {
        String schema = ds.getSchema();
        String dsName = ds.getPoolName().split("__")[0];
        if (TextUtil.isNotEmpty(schema)) {
            lqb.setDefaultSchema(schema);
            lqb.setLiquibaseSchema(schema);
        }
        lqb.setChangeLogParameters(changeLogParams);
        try {
            lqb.afterPropertiesSet(); // Run migrations
            LOG.info("Datasource {} bootstrapped successfully", dsName);
        } catch (LiquibaseException e) {
            throw new DatabaseException(e, "Migration failed for {0}", schema);
        }
    }

    public DataSource get(String tenant) {
        return Objects.requireNonNull((DataSource) dataSources.get(tenant));
    }


}
