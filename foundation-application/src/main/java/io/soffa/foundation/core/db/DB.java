package io.soffa.foundation.core.db;

import javax.sql.DataSource;
import java.time.Duration;

public interface DB {

    void createSchema(String linkId, String schema);

    boolean tenantExists(String tenant);

    DataSource determineTargetDataSource();

    String getTablesPrefix();

    default void configureTenants() {
        // Implementation not required
    }

    default void configureTenantsAsync() {
        // Implementation not required
    }

    default void register(String[] names, boolean migrate) {
        // Implementation not required
    }

    default void withLock(String name, int atMostSeconds, int atLeastSeconds, Runnable runnable) {
        withLock(name, Duration.ofSeconds(atMostSeconds), Duration.ofSeconds(atLeastSeconds), runnable);
    }

    void withLock(String name, Duration atMost, Duration atLeast, Runnable runnable);

}
