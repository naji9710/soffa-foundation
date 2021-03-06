package io.soffa.foundation.data;

public interface TenantAwareDatasource {
    void createSchema(String linkId, String schema);

    void applyMigrations(String tenantId, String changeLogPath);
}
