package io.soffa.foundation.spring.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import lombok.AllArgsConstructor;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

@AllArgsConstructor
public class MetricsRegistryImpl implements MetricsRegistry {

    private final MeterRegistry registry;

    @Override
    public void increment(String counter, double amount, Map<String, Object> tags) {
        registry.counter(counter).increment(amount);
    }

    @Override
    public double counter(String name) {
        try {
            return registry.get(name).counter().count();
        } catch (MeterNotFoundException e) {
            return 0;
        }
    }

    @Override
    public void timed(String name, Duration duration, Map<String, Object> tags) {
        registry.timer(name).record(duration);
    }

    @Override
    public void timed(String name, Map<String, Object> tags, Runnable runnable) {
        registry.timer(name).record(runnable);
    }

    @Override
    public <F> F timed(String name, Map<String, Object> tags, Supplier<F> supplier) {
        return registry.timer(name).record(supplier);
    }

    /*
    private String[] createTags(Map<String, Object> tags) {
        Set<String> r = new HashSet<>();
        for (Map.Entry<String, Object> e : tags.entrySet()) {
            if (e.getValue() != null) {
                String value = e.getValue().toString();
                if (TextUtil.isNotEmpty(value)) {
                    r.add(e.getKey());
                    r.add(value);
                }
            }
        }
        return r.toArray(new String[0]);
    }

     */

}
