package app;

import io.soffa.foundation.actions.ActionDispatcher;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.exceptions.FunctionalException;
import io.soffa.foundation.pubsub.Event;
import io.soffa.foundation.pubsub.PubSubClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {"app.syslogs.enabled=true"})
@ActiveProfiles("test")
public class EventHandleTest {

    @Autowired
    private ActionDispatcher dispatcher;

    @Autowired
    private SysLogRepository sysLogs;

    @SneakyThrows
    @Test
    public void testSysAction() {
        TenantHolder.set("T1");
        long t1InitialCount = sysLogs.count();

        dispatcher.handle(new Event("PingAction").withTenant("T1"));
        dispatcher.handle(new Event("PingAction").withTenant("T1"));
        dispatcher.handle(new Event("PingAction").withTenant("T1"));

        TenantHolder.set("T2");
        long t2InitialCount = sysLogs.count();
        Assertions.assertThrows(FunctionalException.class, () -> {
            dispatcher.handle(new Event("PingAction").withTenant("T2"));
        });

        TenantHolder.set("T1");
        assertEquals(t1InitialCount + 3, sysLogs.count());

        TenantHolder.set("T2");
        assertEquals(t2InitialCount + 1, sysLogs.count());
    }


}
