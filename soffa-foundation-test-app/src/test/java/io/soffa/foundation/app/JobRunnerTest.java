package io.soffa.foundation.app;

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

    @Autowired
    private JobManager jobs;

    @Autowired
    private SysLogRepository sysLogs;

    @SneakyThrows
    @Test
    public void testJobRunner() {
        TenantHolder.set("T1");
        assertNotNull(jobs);
        long initialCount = sysLogs.count();
        Job job = jobs.enqueue("testPing", new Event("PingAction").withTenant("T1"));
        jobs.run(job);
        TestUtil.awaitUntil(5, () -> {
            long count = sysLogs.count();
            return count >= initialCount + 1;
        });
    }

}
