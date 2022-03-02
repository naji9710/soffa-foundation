package com.company.app;

import com.company.app.core.Echo;
import com.company.app.core.Ping;
import io.soffa.foundation.core.context.TenantContextHolder;
import io.soffa.foundation.core.messages.MessageFactory;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import io.soffa.foundation.core.pubsub.MessageHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class EventHandlerTest {

    @Autowired
    private MessageHandler handler;

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

        TenantContextHolder.use("T1", (t1) -> {
            handler.handle(MessageFactory.create(ping)); // automatic tenant
            handler.handle(MessageFactory.create(echo, "Hello"));
            handler.handle(MessageFactory.create(ping));
        });

        assertEquals(pingCount + 2, getCounterValue(ping));
        assertEquals(echoCount + 1, getCounterValue(echo));

    }


}
