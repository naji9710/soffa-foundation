package io.soffa.foundation.data;

public interface DB {

    void createSchema(String linkId, String schema);

    void applyMigrations(String tenantId);

    boolean tenantExists(String tenant);

}
