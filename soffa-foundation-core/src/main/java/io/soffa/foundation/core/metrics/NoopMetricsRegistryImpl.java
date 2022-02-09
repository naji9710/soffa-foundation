package io.soffa.foundation.core.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NoopMetricsRegistryImpl implements MetricsRegistry {

    private static final Map<String, Double> REG = new ConcurrentHashMap<>();

    @Override
    public void increment(String counter, double value) {
        REG.put(counter, REG.getOrDefault(counter, 0d) + 1);
    }
}
