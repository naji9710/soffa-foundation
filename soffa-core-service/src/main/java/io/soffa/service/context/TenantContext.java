package io.soffa.service.context;

import io.soffa.commons.lang.TextUtil;

import java.util.Optional;

public class TenantContext {

    private static final ThreadLocal<String> current = new ThreadLocal<>();

    public synchronized static void set(String value) {
        if (TextUtil.isEmpty(value)) {
            current.remove();
        } else {
            current.set(value);
        }
    }

    public synchronized static void clear() {
        current.remove();
    }

    public static Optional<String> get() {
        return Optional.ofNullable(current.get());
    }

}
