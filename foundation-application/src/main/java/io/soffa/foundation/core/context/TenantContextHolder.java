package io.soffa.foundation.core.context;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.models.TenantId;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TenantContextHolder {

    private static final Logger LOG = Logger.get(TenantContextHolder.class);

    private static final ThreadLocal<String> CURRENT = new InheritableThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Optional<String> get() {
        if (isEmpty(CURRENT.get())) {
            return Optional.empty();
        }
        return Optional.of(CURRENT.get());
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }


    public static void set(TenantId tenantId) {
        if (tenantId == null) {
            set((String)null);
        } else {
            set(tenantId.getValue());
        }
    }


    public static void set(String value) {
        Logger.setTenantId(value);
        if (TextUtil.isEmpty(value)) {
            clear();
        } else {
            CURRENT.set(value);
        }
    }

    public static <T> T useDefault(Supplier<T> supplier) {
        return use(null, supplier);
    }

    public static void use(final String tenantId, Runnable runnable) {
        use(TenantId.of(tenantId), runnable);
    }

    @SneakyThrows
    public static void use(final TenantId tenantId, Runnable runnable) {
        use(tenantId, () -> {
            runnable.run();
            return null;
        });
    }


    @SneakyThrows
    public static <O> O use(final TenantId tenantId, Supplier<O> supplier) {
        String current = CURRENT.get();
        try {
            set(tenantId);
            return supplier.get();
        } finally {
            if (TextUtil.isNotEmpty(current)) {
                LOG.debug("Tenant restored: %s", current);
                CURRENT.set(current);
            }
        }
    }

    @SneakyThrows
    public static void use(final String tenantId, Consumer<TenantId> consumer) {
        use(TenantId.of(tenantId), consumer);
    }

    @SneakyThrows
    public static void use(final TenantId tenantId, Consumer<TenantId> consumer) {
        use(tenantId, () -> consumer.accept(tenantId));
    }


}
