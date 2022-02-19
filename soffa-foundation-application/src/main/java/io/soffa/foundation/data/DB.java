package io.soffa.foundation.data;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface DB {

    void createSchema(String linkId, String schema);

    void applyMigrations(String tenantId);

    boolean tenantExists(String tenant);

    List<Map<String,Object>> query(String ds, String query);

    List<Map<String,Object>> query(String query);

    DataSource determineTargetDataSource();

    String getTablesPrefix();

    default void withLock(String name, int atMostSeconds, int atLeastSeconds, Runnable runnable) {
        withLock(name, Duration.ofSeconds(atMostSeconds), Duration.ofSeconds(atLeastSeconds), runnable);
    }

    void withLock(String name, Duration atMost, Duration atLeast, Runnable runnable);

}
