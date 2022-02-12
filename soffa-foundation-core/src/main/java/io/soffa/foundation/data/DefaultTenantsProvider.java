package io.soffa.foundation.data;

import org.jdbi.v3.core.Handle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultTenantsProvider implements TenantsProvider {

    private final Set<String> items = new HashSet<>();
    private final Map<String,Object> properties = new HashMap<>();

    public DefaultTenantsProvider(Set<String> links) {
        items.addAll(links);
    }

    @Override
    public Set<String> getTenantList(Handle handle) {
        return items;
    }

    @Override
    public boolean exists(String id) {
        return items.contains(id);
    }

    @Override
    public void setProperty(String tenant, String key, Object value) {
        properties.put(tenant + "__" + key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String tenant, String key) {
        return (T)properties.get(tenant + "__" + key);
    }

}
