package io.soffa.foundation.core.context;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.exceptions.FunctionalException;
import io.soffa.foundation.core.model.TenantId;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TenantHolder {

    private static final ThreadLocal<String> CURRENT = new InheritableThreadLocal<>();
    public static boolean hasDefault;


    private TenantHolder() {
    }

    public static void set(TenantId tenantId) {
        if (tenantId == null) {
            set("");
        } else {
            set(tenantId.getValue());
        }
    }

    public static void set(String value) {
        Logger.setTenantId(TenantId.of(value));
        if (TextUtil.isEmpty(value)) {
            clear();
        } else {
            CURRENT.set(value);
        }
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static boolean isEmpty() {
        return TextUtil.isEmpty(CURRENT.get()) && !hasDefault;
    }

    public static boolean isNotEmpty() {
        return !isEmpty();
    }

    public static Optional<String> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static String require() {
        if (CURRENT.get() == null) {
            if (hasDefault) {
                return TenantId.DEFAULT_VALUE;
            }
            throw new FunctionalException("MISSING_TENANT");
        }
        return CURRENT.get();
    }


    public static void use(final String tenantId, Runnable runnable) {
        use(TenantId.of(tenantId), runnable);
    }

    @SneakyThrows
    public static void use(final TenantId tenantId, Runnable runnable) {
        set(tenantId);
        runnable.run();
    }


    @SneakyThrows
    public static <O> O use(final TenantId tenantId, Supplier<O> supplier) {
        set(tenantId);
        return supplier.get();
    }

    @SneakyThrows
    public static void use(final String tenantId, Consumer<TenantId> consumer) {
        use(TenantId.of(tenantId), consumer);
    }

    @SneakyThrows
    public static void use(final TenantId tenantId, Consumer<TenantId> consumer) {
        set(tenantId);
        consumer.accept(tenantId);
    }

}


