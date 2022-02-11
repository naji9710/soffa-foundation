package io.soffa.foundation.core.context;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.exceptions.FunctionalException;

import java.util.Optional;

public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CURRENT = new InheritableThreadLocal<>();

    private RequestContextHolder() {
    }

    public static void set(RequestContext value) {
        if (value == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(value);
            if (value.getTenantId() != null) {
                TenantHolder.set(value.getTenantId());
            }

        }
        Logger.setContext(value);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static boolean isEmpty() {
        return CURRENT.get() == null;
    }

    public static Optional<RequestContext> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static RequestContext require() {
        return Optional.ofNullable(CURRENT.get()).orElseThrow(() -> new FunctionalException("MISSING_NTENAT"));
    }


}
