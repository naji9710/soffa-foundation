package io.soffa.foundation.app;

import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.events.Event;
import io.soffa.foundation.spring.config.jobs.JobManager;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    public void testSysAction() {
        assertNotNull(jobs);
        TenantHolder.set("T1");
        long initialCount = sysLogs.count();
        jobs.enqueue("testPing", new Event("PingAction"));
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
            long count = sysLogs.count();
            return initialCount + 1 ==  count;
        });
        assertEquals(initialCount + 1, sysLogs.count());
    }


}
