package io.soffa.foundation.service.context;

import io.soffa.foundation.commons.lang.TextUtil;

import java.util.Optional;

public final class TenantHolder {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantHolder() {
    }

    public static void set(String value) {
        synchronized (CURRENT) {
            if (TextUtil.isEmpty(value)) {
                CURRENT.remove();
            } else {
                CURRENT.set(value);
            }
        }
    }

    public static void clear() {
        synchronized (CURRENT) {
            CURRENT.remove();
        }
    }

    public static Optional<String> get() {
        return Optional.ofNullable(CURRENT.get());
    }

}
