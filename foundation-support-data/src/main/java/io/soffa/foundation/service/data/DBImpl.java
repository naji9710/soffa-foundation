package io.soffa.foundation.service.data;

import io.soffa.foundation.commons.CollectionUtil;
import io.soffa.foundation.commons.EventBus;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.TenantsLoader;
import io.soffa.foundation.core.context.TenantContextHolder;
import io.soffa.foundation.core.db.DB;
import io.soffa.foundation.core.db.DataSourceConfig;
import io.soffa.foundation.core.db.DataSourceProperties;
import io.soffa.foundation.core.db.DbConfig;
import io.soffa.foundation.core.events.DatabaseReadyEvent;
import io.soffa.foundation.core.models.TenantId;
import io.soffa.foundation.errors.ConfigurationException;
import io.soffa.foundation.errors.InvalidTenantException;
import io.soffa.foundation.errors.NotImplementedException;
import io.soffa.foundation.errors.TechnicalException;
import lombok.SneakyThrows;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.GodClass")
public final class DBImpl extends AbstractDataSource implements ApplicationListener<ContextRefreshedEvent>, DB {

    private static final Logger LOG = Logger.get(DBImpl.class);
    private String tablesPrefix;
    private final String appicationName;
    private String tenanstListQuery;
    private LockProvider lockProvider;
    private static final String TENANT_PLACEHOLDER = "__tenant__";
    private static final String DEFAULT_DS = "default";
    private final ApplicationContext context;
    private static final AtomicReference<String> LOCK = new AtomicReference<>("DB_LOCK");
    private final Map<String, DatasourceInfo> registry = new ConcurrentHashMap<>();

    @SneakyThrows
    public DBImpl(final ApplicationContext context,
                  final DbConfig dbConfig,
                  final String appicationName) {

        super();

        this.context = context;
        this.appicationName = appicationName;
        if (dbConfig != null) {
            this.tenanstListQuery = dbConfig.getTenantListQuery();
            this.tablesPrefix = dbConfig.getTablesPrefix();
            createDatasources(dbConfig.getDatasources());
            this.lockProvider = DBHelper.createLockTable(registry.get(DEFAULT_DS).getDataSource(), this.tablesPrefix);
            applyMirations();
        }
    }

    @Override
    public String getTablesPrefix() {
        return tablesPrefix;
    }

    private void createDatasources(Map<String, DataSourceConfig> datasources) {
        if (datasources == null || datasources.isEmpty()) {
            LOG.warn("No datasources configured for this service.");
        } else {
            for (Map.Entry<String, DataSourceConfig> dbLink : datasources.entrySet()) {
                register(dbLink.getKey(), dbLink.getValue(), false);
            }
            if (!registry.containsKey(DEFAULT_DS)) {
                throw new TechnicalException("No default datasource provided");
            }
        }
    }

    @Override
    public void register(String[] names, boolean migrate) {
        if (!registry.containsKey(TENANT_PLACEHOLDER)) {
            throw new ConfigurationException("No tenant template (__TENANT__) provided, check your config");
        }
        DataSourceConfig tplConfig = registry.get(TENANT_PLACEHOLDER).getConfig();
        for (String name : names) {
            register(name, tplConfig, migrate);
        }
    }

    private void register(String id, DataSourceConfig config, boolean migrate) {
        String sourceId = id.toLowerCase();
        if (registry.containsKey(sourceId)) {
            LOG.warn("Datasource with id {} is already registered", id);
            return;
        }
        String url = config.getUrl().replace(TENANT_PLACEHOLDER, id).replace(TENANT_PLACEHOLDER.toUpperCase(), id);
        if (TENANT_PLACEHOLDER.equalsIgnoreCase(sourceId)) {
            registry.put(id.toLowerCase(), new DatasourceInfo(id, config));
        } else {
            DataSource ds = DBHelper.createDataSource(DataSourceProperties.create(appicationName, id, url), config);
            registry.put(sourceId, new DatasourceInfo(id, config, ds));
            if (migrate) {
                applyMigrations(id);
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) {
        throw new NotImplementedException("Not supported");
    }

    @NotNull
    @Override
    public DataSource determineTargetDataSource() {
        Object lookupKey = determineCurrentLookupKey();
        if (lookupKey != null) {
            lookupKey = lookupKey.toString().toLowerCase();
        }
        if (!registry.containsKey(lookupKey)) {
            throw new InvalidTenantException("%s is not a valid database link", lookupKey);
        }
        LOG.debug("Using datasource: %s", lookupKey);
        return registry.get(lookupKey).getDataSource();
    }

    private Object determineCurrentLookupKey() {
        String linkId = TenantContextHolder.get().orElse(null);
        if (linkId == null) {
            if (registry.containsKey(TenantId.DEFAULT_VALUE)) {
                return TenantId.DEFAULT_VALUE;
            }
            throw new InvalidTenantException("Missing database link. Don't forget to set active tenant with TenantHolder.set()");
        }
        linkId = linkId.toLowerCase();
        if (!registry.containsKey(linkId) && registry.containsKey(TENANT_PLACEHOLDER)) {
            throw new InvalidTenantException("No datasource registered for tenant %s", linkId);
        }
        return linkId;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        configureTenantsAsync();
        EventBus.post(new DatabaseReadyEvent());
    }

    @Override
    public void createSchema(String tenantId, String schema) {
        DataSource ds = registry.get(tenantId.toLowerCase()).getDataSource();
        if (ds == null) {
            throw new TechnicalException("Datasource not registered: " + tenantId);
        }
        Jdbi.create(ds).useHandle(handle -> {
            if (handle.execute("CREATE SCHEMA IF NOT EXISTS " + schema) > 0) {
                LOG.info("New schema created: %s", schema);
            }
        });
    }

    public void applyMigrations(String... datasources) {
        for (String datasource : datasources) {
            applyMigrations(datasource);
        }
    }

    public void applyMigrations(String datasource) {

        if (TENANT_PLACEHOLDER.equals(datasource)) {
            return;
        }

        String linkId = datasource.toLowerCase();
        DatasourceInfo info = registry.get(linkId);
        if (info.isMigrated()) {
            return;
        }
        synchronized (LOCK) {
            withLock("db-migration-" + linkId, 60, 30, () -> {
                String changelogPath = DBHelper.findChangeLogPath(appicationName, info.getConfig());
                if (TextUtil.isNotEmpty(changelogPath)) {
                    DBHelper.applyMigrations(info, changelogPath, tablesPrefix, appicationName);
                }
                info.setMigrated(true);
                LOG.info("Migrations applied for %s", linkId);
            });
        }
    }

    @Override
    public boolean tenantExists(String tenant) {
        return registry.containsKey(tenant.toLowerCase());
    }

    @Override
    public void withLock(String name, Duration atMost, Duration atLeast, Runnable runnable) {
        LockConfiguration config = new LockConfiguration(Instant.now(), name, atMost, atLeast);
        lockProvider.lock(config).ifPresent(simpleLock -> {
            try {
                runnable.run();
            } finally {
                simpleLock.unlock();
            }
        });
    }

    public void applyMirations() {
        registry.keySet().forEach(this::applyMigrations);
    }

    @Override
    public void configureTenants() {
        DataSource defaultDs = registry.get(DEFAULT_DS).getDataSource();

        if (!registry.containsKey(TENANT_PLACEHOLDER)) {
            LOG.debug("No TenantDS provided, skipping tenants migration.");
        }

        final Set<String> tenants = new HashSet<>();
        if (TextUtil.isNotEmpty(tenanstListQuery)) {
            LOG.info("Loading tenants from database");
            Jdbi jdbi = Jdbi.create(defaultDs);
            jdbi.useHandle(handle -> {
                LOG.info("Loading tenants from query: %s", tenanstListQuery);
                List<String> results = handle.createQuery(tenanstListQuery).mapTo(String.class).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(results)) {
                    tenants.addAll(results);
                }
            });
        } else {
            LOG.info("Loading tenants with TenantsLoader");
            try {
                TenantsLoader tenantsLoader = context.getBean(TenantsLoader.class);
                tenants.addAll(tenantsLoader.getTenantList());
            } catch (NoSuchBeanDefinitionException e) {
                LOG.error("No TenantsLoader defined");
            } catch (Exception e) {
                LOG.error("Error loading tenants", e);
            }
        }

        LOG.info("Tenants loaded: %d", tenants.size());
        DatasourceInfo info = registry.get(TENANT_PLACEHOLDER);
        for (String tenant : tenants) {
            register(tenant, info.getConfig(), true);
        }
        LOG.info("Database is now configured");
    }

    @Override
    public void configureTenantsAsync() {
        new Thread(this::configureTenants).start();
    }
}
