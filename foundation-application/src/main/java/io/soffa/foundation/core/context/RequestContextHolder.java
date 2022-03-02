package io.soffa.foundation.core.context;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.http.HttpContextHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.errors.FunctionalException;

import java.util.Optional;

public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CURRENT = new InheritableThreadLocal<>();

    private RequestContextHolder() {
    }

    public static void set(RequestContext value) {
        if (value == null) {
            CURRENT.remove();
            HttpContextHolder.clear();
            Logger.setContext(null);
        } else {
            CURRENT.set(value);
            Logger.setContext(value.getContextMap());
            HttpContextHolder.set(value.getHeaders());
        }
    }

    public static void clear() {
        set(null);
    }

    public static boolean isEmpty() {
        return CURRENT.get() == null;
    }

    public static Optional<RequestContext> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static RequestContext inheritOrCreate() {
        return Optional.ofNullable(CURRENT.get()).orElse(new DefaultRequestContext());
    }

    public static RequestContext require() {
        return Optional.ofNullable(CURRENT.get()).orElseThrow(() -> new FunctionalException("MISSING_CONTEXT"));
    }

}
