package io.soffa.foundation.context;

import com.google.common.base.Preconditions;
import io.soffa.foundation.core.model.TenantId;
import io.soffa.foundation.exceptions.FunctionalException;
import io.soffa.foundation.lang.TextUtil;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class TenantHolder {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();
    private static final ExecutorService SC = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private TenantHolder() {
    }

    public static void set(String value) {
        Preconditions.checkArgument(TextUtil.isNotEmpty(value), "Tenant cannot be empty");
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

    public static boolean isEmpty() {
        return TextUtil.isEmpty(CURRENT.get());
    }

    public static Optional<String> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static String require() {
        return Optional.ofNullable(CURRENT.get()).orElseThrow(() -> new FunctionalException("MISSING_NTENAT"));
    }

    public static void submit(Runnable runnable) {
        submit(require(), runnable);
    }

    public static void submit(final String tenantId, Runnable runnable) {
        SC.submit(() -> {
            TenantHolder.set(tenantId);
            runnable.run();
        });
    }

    @SneakyThrows
    public static void use(final TenantId tenantId, Runnable runnable) {
        if (tenantId == null) {
            runnable.run();
        } else {
            String current = CURRENT.get();
            try {
                set(tenantId.getValue());
                runnable.run();
            } finally {
                set(current);
            }
        }
    }
    @SneakyThrows
    public static<O> O use(final TenantId tenantId, Supplier<O> supplier) {
        if (tenantId == null) {
            return supplier.get();
        } else {
            String current = CURRENT.get();
            try {
                set(tenantId.getValue());
                return supplier.get();
            } finally {
                set(current);
            }
        }
    }
}
