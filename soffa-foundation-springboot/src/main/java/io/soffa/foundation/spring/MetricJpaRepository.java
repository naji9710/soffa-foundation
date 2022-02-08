package io.soffa.foundation.spring;

import io.soffa.foundation.spring.data.entities.MetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricJpaRepository extends JpaRepository<MetricEntity, String> {
}
