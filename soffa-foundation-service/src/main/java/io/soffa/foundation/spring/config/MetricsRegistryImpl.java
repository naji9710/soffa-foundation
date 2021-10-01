package io.soffa.foundation.spring.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.soffa.foundation.metrics.MetricsRegistry;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MetricsRegistryImpl implements MetricsRegistry {

    private final MeterRegistry registry;

    @Override
    public void increment(String counter, double amount) {
        registry.counter(counter).increment(amount);
    }

}
