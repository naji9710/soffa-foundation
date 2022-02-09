package com.company.app.events;

import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.core.actions.MessageHandler;
import io.soffa.foundation.core.data.SysLogRepository;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.exceptions.FakeException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class EventHandleTest {

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private SysLogRepository sysLogs;

    @Test
    public void testEventsHandler() {

        String actionName = "PingAction";

        TenantHolder.use("T1", (t1) -> {
            long initialCount = sysLogs.count();
            messageHandler.onMessage(new Message(actionName)); // automatic tenant
            messageHandler.onMessage(new Message("EchoAction", "Hello"));
            messageHandler.onMessage(new Message(actionName).withTenant(t1)); // explicit tenant
            Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).until(() -> sysLogs.count() == initialCount + 3);
        });

        final AtomicLong t2InitialCount = new AtomicLong();

        TenantHolder.use("T2", (t2) -> {
            t2InitialCount.set(sysLogs.count());
            Assertions.assertThrows(FakeException.class, () -> messageHandler.onMessage(new Message(actionName)));
        });

        TenantHolder.use("T2", () -> {
            Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).until(() -> t2InitialCount.get() + 1 == sysLogs.count());
            assertEquals(t2InitialCount.get() + 1, sysLogs.count());
        });

    }


}
