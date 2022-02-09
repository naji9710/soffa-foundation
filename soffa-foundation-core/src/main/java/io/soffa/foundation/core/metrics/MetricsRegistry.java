package io.soffa.foundation.core.metrics;

public interface MetricsRegistry {

    default void increment(String counter) {
        increment(counter, 1);
    }

    void increment(String counter, double amount);

}
