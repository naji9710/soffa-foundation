package io.soffa.foundation.data;

import org.jdbi.v3.core.Handle;

import java.util.HashSet;
import java.util.Set;

public interface TenantsProvider {

    default Set<String> getTenantList(Handle handle) {
        return new HashSet<>();
    }

    default boolean exists(String id) {
        return false;
    }

    default void setProperty(String tenant, String key, Object value) {
        // No action by default, needs to be overriden
    }

    default <T> T getProperty(String tenant, String key) {
        return null;
    }

}
