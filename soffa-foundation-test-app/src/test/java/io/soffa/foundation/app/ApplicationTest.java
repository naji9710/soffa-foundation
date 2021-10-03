package io.soffa.foundation.app;

import io.soffa.foundation.app.gateway.MessageRepository;
import io.soffa.foundation.commons.IDs;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.test.HttpExpect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {"app.sys-logs.enabled=false"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MessageRepository messages;

    @Test
    public void testActuator() {
        HttpExpect test = new HttpExpect(mvc);
        test.get("/actuator/health").expect().isOK().json("$.status", equalTo("UP"));
    }

    @Test
    public void testController() {
        HttpExpect test = new HttpExpect(mvc);
        test.get("/ping").
            header("X-Application", "TestApp").
            header("X-TenantId", "T1").
            header("X-TraceId", IDs.shortUUID("trace-")).
            header("X-SpanId", IDs.shortUUID("span-")).
            expect().isOK().contentIs("PONG");
    }

    @Test
    public void testCustomRestExceptionHandler() {
        HttpExpect test = new HttpExpect(mvc);
        test.get("/ping").
            header("X-Application", "TestApp").
            header("X-TenantId", "T2").
            header("X-TraceId", IDs.shortUUID("trace-")).
            header("X-SpanId", IDs.shortUUID("span-")).
            expect().is5xxServerError().
            hasJson("$.timestamp").
            hasJson("$.kind").
            hasJson("$.status").
            hasJson("$.message").
            hasJson("$.prod").
            hasJson("$.traceId").
            hasJson("$.spanId").
            hasJson("$.application");
    }

    @Test
    public void testConfig() {
        assertEquals(0L, messages.count());
        TenantHolder.set("T1");
        assertEquals(0L, messages.count());
        TenantHolder.set("T2");
        assertEquals(0L, messages.count());
    }

}
