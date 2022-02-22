package io.soffa.foundation.core;

import java.util.Set;

public interface TenantsLoader {

    TenantsLoader NOOP = () -> null;

    Set<String> getTenantList();

}
