package io.soffa.foundation.core.data;

public interface TenantAwareDatasource {

    void createSchema(String linkId, String schema);

}
