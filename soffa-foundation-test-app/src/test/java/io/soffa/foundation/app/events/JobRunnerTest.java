package io.soffa.foundation.app.events;

import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.events.Event;
import io.soffa.foundation.spring.config.jobs.Job;
import io.soffa.foundation.spring.config.jobs.JobManager;
import io.soffa.foundation.test.TestUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {"app.sys-jobs.enabled=true", "app.sys-logs.enabled=true"})
@ActiveProfiles("test")
public class JobRunnerTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JobManager jobs;

    @Autowired
    private SysLogRepository sysLogs;

    @SneakyThrows
    @Test
    public void testJobRunner() {
        assertNotNull(jobs);
        TenantHolder.use("T1", (t1) -> {
            long initialCount = sysLogs.count();
            Job job = jobs.enqueue("testPing", new Event("PingAction").withTenant(t1));
            jobs.run(job);
            TestUtil.awaitUntil(5, () -> sysLogs.count() > initialCount);
        });
    }

}
