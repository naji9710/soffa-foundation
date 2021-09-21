package io.soffa.service.data;

import com.zaxxer.hikari.HikariDataSource;
import io.soffa.commons.data.CustomPhysicalNamingStrategy;
import io.soffa.commons.data.DataSourceProperties;
import io.soffa.commons.exceptions.DatabaseException;
import io.soffa.commons.exceptions.TechnicalException;
import io.soffa.commons.lang.TextUtil;
import io.soffa.commons.logging.Logger;
import io.soffa.service.context.TenantContext;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TenantAwareDatasource extends AbstractRoutingDataSource {

    private static final Logger logger = Logger.create(TenantAwareDatasource.class);
    private final Map<Object, Object> dataSources = new ConcurrentHashMap<>();
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final String tablesPrefix;
    private final String appicationName;

    @Override
    protected Object determineCurrentLookupKey() {
        String linkId = TenantContext.get().orElseThrow(() -> new DatabaseException("Missing database link"));
        if (!dataSources.containsKey(linkId)) {
            throw new DatabaseException("{0} is not a valid database link", linkId);
        }
        return linkId;
    }

    public String getTablesPrefix() {
        return tablesPrefix;
    }

    @SneakyThrows
    public TenantAwareDatasource(final Map<String, String> links,
                                 final String tablesPrefix,
                                 final String appicationName) {

        setLenientFallback(false);
        CustomPhysicalNamingStrategy.tablePrefix = tablesPrefix;
        if (links == null || links.isEmpty()) {
            throw new TechnicalException("No db link provided");
        }
        links.forEach((name, url) -> {
            dataSources.put(name, createDataSource(DataSourceProperties.create(name, url.trim())));
        });
        super.setTargetDataSources(dataSources);
        //
        // dataSourceList = dataSources.values().stream().map(o -> (DataSource) o).collect(Collectors.toList());
        this.tablesPrefix = tablesPrefix;
        this.appicationName = appicationName;
    }


    @SneakyThrows
    private synchronized DataSource createDataSource(DataSourceProperties config) {

        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder
                .create().driverClassName(config.getDriverClassName())
                .username(config.getUsername())
                .password(config.getPassword())
                .url(config.getUrl())
                .build();

        if (TextUtil.isNotEmpty(config.getSchema())) {
            logger.debug("Creating datasource for {} with schema = {}", config.getUrl(), config.getSchema());
        } else {
            logger.debug("Creating datasource for {}", config.getUrl(), config.getSchema());
        }

        dataSource.setConnectionTestQuery("select 1");
        dataSource.setIdleTimeout(60000);
        dataSource.setMaximumPoolSize(4);
        dataSource.setMinimumIdle(0);
        dataSource.setValidationTimeout(10000);
        //dataSource.setPoolName(config.getName() + "-" + RandomUtils.nextInt());

        if (config.hasSchema()) {
            dataSource.setSchema(config.getSchema());
        }
        return dataSource;
    }

    public void applyMigrations() {
        String changeLog = "/db/changelog/" + appicationName + ".xml";
        for (Object value : dataSources.values()) {
            applyMigrations((DataSource) value, changeLog);
        }
    }

    public void applyMigrations(String tenant) {
        applyMigrations(get(tenant), "/db/changelog/" + appicationName + ".xml");
    }

    public void applyMigrations(String tenant, String changeLogPath) {
        applyMigrations(get(tenant), changeLogPath);
    }

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
        HikariDataSource ds = (HikariDataSource) dataSource;
        String schema = ds.getSchema();
        if (TextUtil.isNotEmpty(schema)) {
            lqb.setDefaultSchema(schema);
            lqb.setLiquibaseSchema(schema);
        }
        lqb.setChangeLogParameters(changeLogParams);

        try {
            lqb.afterPropertiesSet(); // Run migrations
            if (TextUtil.isNotEmpty(schema)) {
                logger.info("Datasource: {0}.{1} bootstrapped successfully", appicationName, schema);
            } else {
                logger.info("Datasource: {0} bootstrapped successfully", appicationName);
            }
        } catch (LiquibaseException e) {
            if (TextUtil.isNotEmpty(schema)) {
                throw new DatabaseException(e, "Migration failed for {0}.{1}", appicationName, schema);
            } else {
                throw new DatabaseException(e, "Migration failed for {0}", appicationName);
            }
        }
    }

    public DataSource get(String tenant) {
        return Objects.requireNonNull((DataSource) dataSources.get(tenant));
    }
}
