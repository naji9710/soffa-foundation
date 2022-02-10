package io.soffa.foundation.core.metrics;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.exceptions.ManagedException;
import io.soffa.foundation.exceptions.TechnicalException;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

public interface MetricsRegistry {

    String FAILED_SUFFIX = "_failed";
    String DURATION_SUFFIX = "_duration";

    default void increment(String counter) {
        increment(counter, 1, ImmutableMap.of());
    }

    default void increment(String counter, Map<String, Object> tags) {
        increment(counter, 1, tags);
    }

    default <T> T track(String prefix, Map<String, Object> tags, Supplier<T> supplier) {
        try {
            T result = timed(prefix + DURATION_SUFFIX, tags, supplier);
            increment(prefix, tags);
            return result;
        } catch (Exception e) {
            increment(prefix + FAILED_SUFFIX, tags);
            if (e instanceof ManagedException) {
                throw e;
            } else {
                throw new TechnicalException(e.getMessage(), e);
            }
        }
    }

    default void track(String prefix, Map<String, Object> tags, Runnable runnable) {
        try {
            timed(prefix + DURATION_SUFFIX, tags, runnable);
            increment(prefix, tags);
        } catch (Exception e) {
            increment(prefix + FAILED_SUFFIX, tags);
            if (e instanceof ManagedException) {
                throw e;
            } else {
                throw new TechnicalException(e.getMessage(), e);
            }
        }
    }

    void increment(String counter, double amount, Map<String, Object> tags);

    double counter(String name);

    void timed(String name, Duration duration, Map<String, Object> tags);

    void timed(String name, Map<String, Object> tags, Runnable runnable);

    <F> F timed(String name, Map<String, Object> tags, Supplier<F> supplier);
}
