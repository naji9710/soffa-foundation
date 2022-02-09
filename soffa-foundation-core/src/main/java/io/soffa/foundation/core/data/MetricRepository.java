package io.soffa.foundation.core.data;

import io.soffa.foundation.core.data.entities.Metric;

public interface MetricRepository {

    void save(Metric value);

    long count();

}
