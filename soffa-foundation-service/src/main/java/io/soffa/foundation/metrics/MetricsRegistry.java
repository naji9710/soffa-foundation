package io.soffa.foundation.metrics;

public interface MetricsRegistry {

    default void increment(String counter) {
        increment(counter, 1);
    }

    void increment(String counter, double amount);

}
