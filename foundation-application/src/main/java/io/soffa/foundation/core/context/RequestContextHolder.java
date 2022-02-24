package io.soffa.foundation.core.context;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.commons.http.HttpContextHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.models.TenantId;
import io.soffa.foundation.errors.FunctionalException;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CONTEXT = new InheritableThreadLocal<>();
    private static final ThreadLocal<String> TENANT_BACKUP = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    public static Optional<String> getTenant() {
        if (CONTEXT.get() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(CONTEXT.get().getTenantId());
    }

    public static void set(RequestContext value) {
        if (value == null) {
            CONTEXT.remove();
            HttpContextHolder.clear();
            Logger.setContext(null);
        } else {
            CONTEXT.set(value);
            Logger.setContext(value.getContextMap());
            HttpContextHolder.set(value.getHeaders());
        }
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static boolean isEmpty() {
        return CONTEXT.get() == null;
    }

    public static Optional<RequestContext> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static RequestContext inheritOrCreate() {
        return Optional.ofNullable(CONTEXT.get()).orElse(new DefaultRequestContext());
    }

    public static RequestContext require() {
        return Optional.ofNullable(CONTEXT.get()).orElseThrow(() -> new FunctionalException("MISSING_TENANT"));
    }

    public static void setTenant(TenantId tenantId) {
        if (tenantId == null) {
            setTenant("");
        } else {
            setTenant(tenantId.getValue());
        }
    }

    private static void restoreTenant() {
        String value = TENANT_BACKUP.get();
        if (TextUtil.isNotEmpty(value)) {
            setTenantInternal(value, false);
        }
    }

    public static void setTenant(String value) {
        setTenantInternal(value, true);
    }

    public static void setTenantInternal(String value, boolean backup) {
        TENANT_BACKUP.set(getTenant().orElse(null));
        Logger.setTenantId(value);
        if (TextUtil.isEmpty(value)) {
            clear();
        } else {
            if (CONTEXT.get() == null) {
                set(new DefaultRequestContext());
            }
            CONTEXT.get().setTenantId(value);
        }
    }

    /*
    public static String require() {
        if (CONTEXT.get() == null) {
            if (hasDefault) {
                return TenantId.DEFAULT_VALUE;
            }
            throw new FunctionalException("MISSING_TENANT");
        }
        return CONTEXT.get();
    }
*/

    public static <T> T useDefaultTenant(Supplier<T> supplier) {
        return useTenant(null, supplier);
    }

    public static void useTenant(final String tenantId, Runnable runnable) {
        useTenant(TenantId.of(tenantId), runnable);
    }

    @SneakyThrows
    public static void useTenant(final TenantId tenantId, Runnable runnable) {
        try {
            setTenant(tenantId);
            runnable.run();
        }finally {
            restoreTenant();
        }
    }


    @SneakyThrows
    public static <O> O useTenant(final TenantId tenantId, Supplier<O> supplier) {
        try {
            setTenant(tenantId);
            return supplier.get();
        }finally {
            restoreTenant();
        }
    }

    @SneakyThrows
    public static void useTenant(final String tenantId, Consumer<TenantId> consumer) {
        useTenant(TenantId.of(tenantId), consumer);
    }

    @SneakyThrows
    public static void useTenant(final TenantId tenantId, Consumer<TenantId> consumer) {
        try {
            setTenant(tenantId);
            consumer.accept(tenantId);
        }finally {
            restoreTenant();
        }
    }


}
