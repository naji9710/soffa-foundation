package com.company.app.events;

import io.soffa.foundation.core.context.TenantHolder;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.core.metrics.CoreMetrics;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import io.soffa.foundation.spring.config.jobs.Job;
import io.soffa.foundation.spring.config.jobs.JobManager;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {"app.jobs.enabled=true"})
@ActiveProfiles("test")
public class JobRunnerTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JobManager jobs;

    @Autowired
    private MetricsRegistry metrics;

    @SneakyThrows
    @Test
    public void testJobRunner() {
        double jobsCount = metrics.counter(CoreMetrics.JOBS);
        assertNotNull(jobs);
        TenantHolder.use("T1", (t1) -> {
            Job job = jobs.enqueue("testPing", new Message("Ping").withTenant(t1));
            jobs.run(job);
        });
        assertEquals(jobsCount+1, metrics.counter(CoreMetrics.JOBS));
    }

}
