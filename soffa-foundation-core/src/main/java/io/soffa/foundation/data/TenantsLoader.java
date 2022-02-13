package io.soffa.foundation.data;

import io.soffa.foundation.messages.BinaryClient;

import java.util.HashSet;
import java.util.Set;

public interface TenantsLoader {

    TenantsLoader NOOP = new TenantsLoader() {};

    default Set<String> getTenantList(BinaryClient client) {
        return new HashSet<>();
    }

}
