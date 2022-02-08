package io.soffa.foundation;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.commons.ExecutorHelper;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.data.MetricRepository;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.data.entities.Metric;
import io.soffa.foundation.data.entities.SysLog;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class DataSourceTest {

    @Autowired
    private SysLogRepository sysLogs;

    @Autowired
    private MetricRepository metricsRepo;

    @SneakyThrows
    @Test
    public void testDataSource() {
        TenantHolder.set("T2");
        assertEquals(0, metricsRepo.count());
        metricsRepo.save(new Metric("metric.test.001", 1.0));
        assertEquals(1, metricsRepo.count());

        TenantHolder.set("T3");
        Assertions.assertThrows(Exception.class, () -> sysLogs.count());

        final int t1Count = RandomUtils.nextInt(10, 50);
        final int t2Count = RandomUtils.nextInt(10, 50);

        Map<String, Integer> links = ImmutableMap.of(
            "T1", t1Count,
            "T2", t2Count
        );

        final CountDownLatch latch = new CountDownLatch(t1Count + t2Count);

        for (final Map.Entry<String, Integer> e : links.entrySet()) {

            final String tenant = e.getKey();
            TenantHolder.set(tenant);
            assertEquals(0, sysLogs.count());

            for (int i = 0; i < e.getValue(); i++) {
                ExecutorHelper.execute(() -> {
                    TenantHolder.set(tenant);
                    sysLogs.save(new SysLog("event", IdGenerator.shortUUID()));
                    latch.countDown();
                });
            }
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        for (
            final Map.Entry<String, Integer> e : links.entrySet()) {
            TenantHolder.set(e.getKey());
            assertEquals(e.getValue(), (int) sysLogs.count());
        }

    }

}
