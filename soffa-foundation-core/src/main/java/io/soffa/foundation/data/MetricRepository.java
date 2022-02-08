package io.soffa.foundation.data;

import io.soffa.foundation.data.entities.Metric;

public interface MetricRepository {

    void save(Metric value);

    long count();

}
