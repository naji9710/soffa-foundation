package io.soffa.foundation.service.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariDataSource;
import io.soffa.foundation.commons.CollectionUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.config.DataSourceConfig;
import io.soffa.foundation.config.DbConfig;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.data.DataSourceProperties;
import io.soffa.foundation.data.TenantAwareDatasource;
import io.soffa.foundation.data.TenantsProvider;
import io.soffa.foundation.exceptions.DatabaseException;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.model.TenantId;
import io.soffa.foundation.service.state.DatabasePlane;
import lombok.SneakyThrows;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class TenantAwareDatasourceImpl extends AbstractRoutingDataSource implements ApplicationListener<ContextRefreshedEvent>, TenantAwareDatasource {

    public static final String NONE = "NONE";
    private static final Logger LOG = Logger.get(TenantAwareDatasourceImpl.class);
    private final Map<Object, Object> dataSources = new ConcurrentHashMap<>();
    private String tablesPrefix = "";
    private final String appicationName;
    private boolean appicationStarted;
    private final TenantsProvider tenantsProvider;
    private static final String TENANT_PLACEHOLDER = "__TENANT__";
    private static final String TENANT_WILDCARD = "__TENANT__";
    private static final String DEFAULT_DS = "default";
    private final DbConfig dbConfig;
    private static final Map<String, Boolean> MIGRATED = new ConcurrentHashMap<>();
    private final Map<DataSource, DataSourceConfig> dsConfigs = new ConcurrentHashMap<>();
    private final DatabasePlane dbState;
    private final ApplicationEventPublisher publisher;


    @SneakyThrows
    public TenantAwareDatasourceImpl(final TenantsProvider tenantsProvider,
                                     final DatabasePlane dbState,
                                     final DbConfig dbConfig,
                                     final String appicationName,
                                     final ApplicationEventPublisher publisher) {

        super();

        Preconditions.checkNotNull(dbState, "DatabasePlane is required");

        this.publisher = publisher;
        this.dbState = dbState;
        this.tenantsProvider = tenantsProvider;
        this.dbConfig = dbConfig;
        this.appicationName = appicationName;
        setTablesPrefix(dbConfig.getTablesPrefix());
        setLenientFallback(false);

        createDatasources();
        TenantHolder.hasDefault = dataSources.containsKey(TenantId.DEFAULT_VALUE);
        super.setTargetDataSources(ImmutableMap.copyOf(dataSources));

        boolean hasMigrations = false;
        for (Map.Entry<Object, Object> entry : dataSources.entrySet()) {
            if (checkMigrations((DataSource) entry.getValue())) {
                dbState.setPending();
                hasMigrations = true;
                break;
            }
        }

        if (!hasMigrations) {
            dbState.setReady();
        }
        createLockTable();
    }

    private void createDatasources() {
        if (dbConfig.getDatasources().isEmpty()) {
            throw new TechnicalException("No db link provided");
        }
        for (Map.Entry<String, DataSourceConfig> dbLink : dbConfig.getDatasources().entrySet()) {
            if (!TENANT_WILDCARD.equalsIgnoreCase(dbLink.getKey())) {
                registerDatasource(dbLink.getKey(), dbLink.getValue(), false);
            }
        }
        if (!dataSources.containsKey(DEFAULT_DS)) {
            throw new TechnicalException("No default datasource provided");
        }
        dataSources.put(NONE, new MockDataSource());
    }

    private void setTablesPrefix(String tablesPrefix) {
        if (TextUtil.isNotEmpty(tablesPrefix)) {

            String value = TextUtil.trimToEmpty(tablesPrefix)
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+$", "_").trim();

            if (!value.endsWith("_")) {
                value += "_";
            }
            this.tablesPrefix = value;
            CustomPhysicalNamingStrategy.tablePrefix = value;
        }
    }

    private void registerDatasource(String id, DataSourceConfig link, boolean migrate) {
        if (dataSources.containsKey(id)) {
            LOG.warn("Datasource with id {} is already registered", id);
            return;
        }
        String url = link.getUrl();
        if (url.contains(TENANT_PLACEHOLDER)) {
            url = url.replace(TENANT_PLACEHOLDER, id);
        }
        DataSource ds = DbHelper.createDataSource(DataSourceProperties.create(id, url), link);
        dsConfigs.put(ds, link);
        dataSources.put(id, ds);
        if (migrate) {
            applyMigrations(ds);
        }
    }

    @NotNull
    @Override
    protected DataSource determineTargetDataSource() {
        Object lookupKey = determineCurrentLookupKey();
        if (!dataSources.containsKey(lookupKey)) {
            throw new DatabaseException("%s is not a valid database link", lookupKey);
        }
        return (DataSource) dataSources.get(lookupKey);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String linkId = TenantHolder.get().orElse(null);
        if (linkId == null) {
            if (dataSources.containsKey(TenantId.DEFAULT_VALUE)) {
                return TenantId.DEFAULT_VALUE;
            } else if (!appicationStarted) {
                return NONE;
            }
            throw new DatabaseException("Missing database link. Don't forget to set active tenant with TenantHolder.set()");
        }
        if (!dataSources.containsKey(linkId) && dbConfig.getDatasources().containsKey(TENANT_WILDCARD)) {
            registerDatasource(linkId, dbConfig.getDatasources().get(TENANT_WILDCARD), true);
            super.setTargetDataSources(ImmutableMap.copyOf(dataSources));
        }
        return linkId;
    }

    public DataSource getDefault() {
        boolean hasOneItem = dataSources.size() == 1;
        if (hasOneItem) {
            return (DataSource) dataSources.values().iterator().next();
        }
        if (dataSources.containsKey(TenantId.DEFAULT_VALUE)) {
            return (DataSource) dataSources.get(TenantId.DEFAULT_VALUE);
        }
        throw new TechnicalException("No default datasource registered");
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (dbState.isPending()) {
            new Thread(() -> {
                try {
                    configure();
                    dbState.setReady();
                    publisher.publishEvent(new DatabaseReadyEvent(event.getApplicationContext()));
                } catch (Exception e) {
                    dbState.setFailed(e.getMessage());
                    logger.fatal("Database migration has failed: " + e.getMessage(), e);
                }
            }).start();
        } else {
            publisher.publishEvent(new DatabaseReadyEvent(event.getApplicationContext()));
        }
        appicationStarted = true;
    }

    @Override
    public void createSchema(String tenantId, String schema) {
        DataSource ds = get(tenantId);
        if (ds == null) {
            throw new TechnicalException("Datasource not registered: " + tenantId);
        }
        Jdbi jdbi = Jdbi.create(ds);
        jdbi.useHandle(handle -> {
            handle.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        });
        LOG.info("New schema created: %s", schema);
    }

    public void applyMigrations() {
        for (Map.Entry<Object, Object> entry : dataSources.entrySet()) {
            applyMigrations((DataSource) entry.getValue());
        }
    }

    private void applyMigrations(DataSource dataSource) {
        if (dataSource instanceof MockDataSource) {
            return;
        }
        if (dataSource instanceof HikariDataSource) {
            applyMigrations((HikariDataSource) dataSource);
        } else {
            throw new TechnicalException("Non HikariDatasource migrations  not supported");
        }
    }

    private boolean checkMigrations(DataSource dataSource) {
        DataSourceConfig link = dsConfigs.get(dataSource);
        return TextUtil.isNotEmpty(findChangeLogPath(link));
    }

    private String findChangeLogPath(DataSourceConfig link) {
        String changelogPath = null;
        boolean noMigration = "false".equals(link.getMigration()) || "no".equals(link.getMigration());
        if (!noMigration) {
            if (TextUtil.isNotEmpty(link.getMigration()) && !"true".equals(link.getMigration())) {
                changelogPath = "/db/changelog/" + link.getMigration() + ".xml";
            } else {
                changelogPath = "/db/changelog/" + appicationName + ".xml";
            }
            if (TextUtil.isNotEmpty(changelogPath)) {
                ResourceLoader resourceLoader = new DefaultResourceLoader();
                if (!resourceLoader.getResource(changelogPath).exists()) {
                    throw new TechnicalException("Changelog file not found: " + changelogPath);
                }
            }
        }
        return changelogPath;
    }

    private void applyMigrations(HikariDataSource dataSource) {
        DataSourceConfig link = dsConfigs.get(dataSource);
        if (MIGRATED.containsKey(appicationName + "." + link.getName())) {
            return;
        }
        String changelogPath = findChangeLogPath(link);
        if (TextUtil.isNotEmpty(changelogPath)) {
            DbHelper.applyMigrations(dataSource, changelogPath, tablesPrefix, appicationName);
        }
        MIGRATED.put(appicationName + "." + link.getName(), true);
    }

    public DataSource get(String tenant) {
        return Objects.requireNonNull((DataSource) dataSources.get(tenant));
    }

    @SneakyThrows
    private void createLockTable() {
        DataSource defaultDs = (DataSource) dataSources.get(DEFAULT_DS);
        LockProvider lockProvider = new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(defaultDs))
            .withTableName(tablesPrefix + "shedlock")
            .usingDbTime()
            .build());
        try {
            Jdbi.create(defaultDs).useTransaction(handle -> {
                handle.execute("CREATE TABLE IF NOT EXISTS " + tablesPrefix + "shedlock(name VARCHAR(64) NOT NULL, lock_until TIMESTAMP NOT NULL, locked_at TIMESTAMP NOT NULL, locked_by VARCHAR(255) NOT NULL, PRIMARY KEY (name))");
            });
        } catch (Exception e) {
            // Will ignore because the table might have been created by another instance of the service
            LOG.warn(e.getMessage(), e);
        }
        dbState.setLockProvider(lockProvider);
    }

    public void configure() {
        DataSource defaultDs = (DataSource) dataSources.get(DEFAULT_DS);
        dbState.withLock("db-migration", 630, 30, () -> {
            // Migrate static tenants
            for (Map.Entry<Object, Object> entry : dataSources.entrySet()) {
                applyMigrations((DataSource) entry.getValue());
            }

            DataSourceConfig tenantDs = dbConfig.getDatasources().get(TENANT_WILDCARD);
            boolean hasTenantDs = tenantDs != null;
            final Set<String> tenants = new HashSet<>();
            if (hasTenantDs) {

                // DataSource defaultDs = (DataSource) dataSources.get(DEFAULT_DS);
                Jdbi jdbi = Jdbi.create(defaultDs);
                jdbi.useHandle(handle -> {
                    String query = dbConfig.getTenantListQuery();
                    if (TextUtil.isNotEmpty(query)) {
                        LOG.info("Loading tenants from query: %s", query);
                        List<String> results = handle.createQuery(query).mapTo(String.class).collect(Collectors.toList());
                        if (CollectionUtil.isNotEmpty(results)) {
                            tenants.addAll(results);
                        }
                    } else {
                        tenants.addAll(tenantsProvider.getTenantList(handle));
                    }
                });

                for (String tenant : tenants) {
                    if (!DEFAULT_DS.equalsIgnoreCase(tenant) && !TENANT_WILDCARD.equalsIgnoreCase(tenant)) {
                        registerDatasource(tenant, dbConfig.getDatasources().get(TENANT_WILDCARD), true);
                    }
                }
            }
        });
    }
}
