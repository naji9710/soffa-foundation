package io.soffa.foundation.spring.config;

import io.soffa.foundation.core.data.MetricRepository;
import io.soffa.foundation.core.data.entities.Metric;
import io.soffa.foundation.spring.MetricJpaRepository;
import io.soffa.foundation.spring.data.entities.MetricEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class MetricRepositoryImpl implements MetricRepository {

    private final MetricJpaRepository repo;

    @Override
    @Transactional
    public void save(Metric value) {
        repo.save(MetricEntity.of(value));
    }

    @Override
    public long count() {
        return repo.count();
    }
}
