package io.soffa.foundation.metrics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnMissingBean(MetricsRegistry.class)
public class NoopMetricsRegistryImpl implements MetricsRegistry{

    private static final Map<String,Double> REG = new ConcurrentHashMap<>();

    @Override
    public void increment(String counter, double value) {
        REG.put(counter, REG.getOrDefault(counter, 0d) + 1);
    }
}
