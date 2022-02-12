package com.company.app;

import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.messages.Message;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.service.config.jobs.Job;
import io.soffa.foundation.service.config.jobs.JobManager;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

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
        double pingCount = countPing();
        assertNotNull(jobs);
        TenantHolder.use("T1", (t1) -> {
            Job job = jobs.enqueue("testPing", new Message("Ping").withTenant(t1));
            jobs.run(job);
        });

        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).until(() -> countPing() == pingCount + 1);
    }

    private double countPing() {
        return metrics.globalCounter("app_operation_ping");
    }

}
