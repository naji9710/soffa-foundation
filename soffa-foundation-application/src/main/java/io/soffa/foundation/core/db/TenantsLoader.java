package io.soffa.foundation.core.db;

import java.util.HashSet;
import java.util.Set;

public interface TenantsLoader {

    TenantsLoader NOOP = new TenantsLoader() {};

    default Set<String> getTenantList() {
        return new HashSet<>();
    }

}
