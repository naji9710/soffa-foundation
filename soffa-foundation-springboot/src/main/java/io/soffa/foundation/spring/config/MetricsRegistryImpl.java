package io.soffa.foundation.spring.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Component
@AllArgsConstructor
@ConditionalOnBean(MeterRegistry.class)
public class MetricsRegistryImpl implements MetricsRegistry {

    private final MeterRegistry registry;

    @Override
    public void increment(String counter, double amount, Map<String,Object> tags) {
        registry.counter(counter, createTags(tags)).increment(amount);
    }

    @Override
    public void timed(String name, Duration duration, Map<String,Object> tags) {
        registry.timer(name, createTags(tags)).record(duration);
    }

    @Override
    public void timed(String name, Map<String,Object> tags, Runnable runnable) {
        registry.timer(name, createTags(tags)).record(runnable);
    }

    @Override
    public <F> F timed(String name, Map<String,Object> tags, Supplier<F> supplier) {
        return registry.timer(name, createTags(tags)).record(supplier);
    }

    private String[] createTags(Map<String,Object> tags) {
        Set<String> r = new HashSet<>();
        for (Map.Entry<String, Object> e : tags.entrySet()) {
            if (e.getValue()!=null) {
                String value = e.getValue().toString();
                if (TextUtil.isNotEmpty(value)) {
                    r.add(e.getKey());
                    r.add(value);
                }
            }
        }
        return r.toArray(new String[0]);
    }

}
