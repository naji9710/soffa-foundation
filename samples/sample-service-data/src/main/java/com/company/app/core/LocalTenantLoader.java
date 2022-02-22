package com.company.app.core;

import com.google.common.collect.ImmutableSet;
import io.soffa.foundation.core.TenantsLoader;

import javax.inject.Named;
import java.util.Set;

@Named
public class LocalTenantLoader implements TenantsLoader {

    @Override
    public Set<String> getTenantList() {
        return ImmutableSet.of("tx001");
    }
}
