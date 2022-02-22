package io.soffa.foundation.application.context;

import io.soffa.foundation.application.RequestContext;
import io.soffa.foundation.application.model.TenantId;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.commons.http.HttpContextHolder;
import io.soffa.foundation.errors.FunctionalException;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CONTEXT = new InheritableThreadLocal<>();
    private static final ThreadLocal<String> TENANT = new InheritableThreadLocal<>();

    private RequestContextHolder() {
    }

    public static Optional<String> getTenant() {
        return Optional.ofNullable(TENANT.get());
    }

    public static void set(RequestContext value) {
        if (value == null) {
            CONTEXT.remove();
            HttpContextHolder.clear();
            Logger.setContext(null);
        } else {
            CONTEXT.set(value);
            if (value.getTenantId() != null) {
                TENANT.set(value.getTenantId());
            }
            Logger.setContext(value.getContextMap());
            HttpContextHolder.set(value.getHeaders());
        }
    }

    public static void clear() {
        CONTEXT.remove();
        TENANT.remove();
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

    public static void setTenant(String value) {
        Logger.setTenantId(value);
        if (TextUtil.isEmpty(value)) {
            clear();
        } else {
            TENANT.set(value);
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

    public static void useTenant(final String tenantId, Runnable runnable) {
        useTenant(TenantId.of(tenantId), runnable);
    }

    @SneakyThrows
    public static void useTenant(final TenantId tenantId, Runnable runnable) {
        setTenant(tenantId);
        runnable.run();
    }


    @SneakyThrows
    public static <O> O useTenant(final TenantId tenantId, Supplier<O> supplier) {
        setTenant(tenantId);
        return supplier.get();
    }

    @SneakyThrows
    public static void useTenant(final String tenantId, Consumer<TenantId> consumer) {
        useTenant(TenantId.of(tenantId), consumer);
    }

    @SneakyThrows
    public static void useTenant(final TenantId tenantId, Consumer<TenantId> consumer) {
        setTenant(tenantId);
        consumer.accept(tenantId);
    }


}
