package com.company.app.events;

import com.company.app.core.Echo;
import com.company.app.core.Ping;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.application.messages.Message;
import io.soffa.foundation.application.metrics.MetricsRegistry;
import io.soffa.foundation.operation.MessagesHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class EventHandleTest {

    @Autowired
    private MessagesHandler handler;

    @Autowired
    private MetricsRegistry metricsRegistry;

    private double getCounterValue(String op) {
        return metricsRegistry.counter("app_operation_" + op);
    }

    @Test
    public void testEventsHandler() {
        String ping = Ping.class.getName();
        String echo = Echo.class.getName();

        double pingCount = getCounterValue(ping);
        double echoCount = getCounterValue(Echo.class.getName());

        TenantHolder.use("T1", (t1) -> {
            handler.onMessage(new Message(ping)); // automatic tenant
            handler.onMessage(new Message(echo, "Hello"));
            handler.onMessage(new Message(ping).withTenant(t1)); // explicit tenant
        });

        assertEquals(pingCount + 2, getCounterValue(ping));
        assertEquals(echoCount + 1, getCounterValue(echo));
    }


}
