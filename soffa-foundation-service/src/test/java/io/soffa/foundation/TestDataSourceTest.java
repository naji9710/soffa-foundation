
package io.soffa.foundation;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.data.SysLog;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.support.Generator;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {"app.syslogs.enabled=true"})
@ActiveProfiles("test")
public class TestDataSourceTest {

    @Autowired
    private SysLogRepository sysLogs;

    @SneakyThrows
    @Test
    public void testDataSource() {
        TenantHolder.set("T3");
        Assertions.assertThrows(Exception.class, () -> {
            sysLogs.count();
        });


        Map<String, Integer> links = ImmutableMap.of(
                "primary", RandomUtils.nextInt(100, 200),
                "T1", RandomUtils.nextInt(100, 300),
                "T2", RandomUtils.nextInt(100, 500)
        );

        final CountDownLatch latch = new CountDownLatch(links.get("primary") + links.get("T1") + links.get("T2"));

        for (final Map.Entry<String, Integer> e : links.entrySet()) {

            TenantHolder.set(e.getKey());
            assertEquals(0, sysLogs.count());

            for (int i = 0; i < e.getValue(); i++) {
                TenantHolder.submit(() -> {
                    sysLogs.save(new SysLog("event", Generator.shortId()));
                    latch.countDown();
                });
            }
        }
        latch.await();

        for (final Map.Entry<String, Integer> e : links.entrySet()) {
            TenantHolder.set(e.getKey());
            assertEquals(e.getValue(), (int) sysLogs.count());
        }

    }

}
