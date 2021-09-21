
package io.soffa.spring;

import com.google.common.collect.ImmutableMap;
import io.soffa.commons.support.Generator;
import io.soffa.service.context.TenantContext;
import io.soffa.spring.data.EventEntity;
import io.soffa.spring.data.EventRepository;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionException;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class TestDataSourceTest {

    @Autowired
    private EventRepository eventsRepo;

    @SneakyThrows
    @Test
    public void testDataSource() {
        TenantContext.set("T3");
        Assertions.assertThrows(TransactionException.class, () -> {
            eventsRepo.count();
        });

        final ExecutorService threads = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Map<String, Integer> links = ImmutableMap.of(
                "primary", RandomUtils.nextInt(100, 2000),
                "T1", RandomUtils.nextInt(100, 3000),
                "T2", RandomUtils.nextInt(100, 5000)
        );

        final CountDownLatch latch = new CountDownLatch(links.get("primary") + links.get("T1") + links.get("T2"));

        for (final Map.Entry<String, Integer> e : links.entrySet()) {

            TenantContext.set(e.getKey());
            eventsRepo.deleteAll();
            assertEquals(0, eventsRepo.count());

            for (int i = 0; i < e.getValue(); i++) {
                threads.submit(() -> {
                    TenantContext.set(e.getKey());
                    eventsRepo.save(new EventEntity(Generator.shortId(), RandomStringUtils.random(15), new Date()));
                    latch.countDown();
                });
            }
        }
        latch.await();

        for (final Map.Entry<String, Integer> e : links.entrySet()) {
            TenantContext.set(e.getKey());
            assertEquals(e.getValue(), (int) eventsRepo.count());
        }

    }

}
