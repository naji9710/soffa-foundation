package io.soffa.foundation.context;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.exceptions.FunctionalException;
import io.soffa.foundation.logging.Logger;

import java.util.Optional;

public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CURRENT = new InheritableThreadLocal<>();

    private RequestContextHolder() {
    }

    public static void set(RequestContext value) {
        synchronized (CURRENT) {
            if (value == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(value);
            }
        }
        Logger.setContext(value);
    }

    public static void clear() {
        synchronized (CURRENT) {
            CURRENT.remove();
        }
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
