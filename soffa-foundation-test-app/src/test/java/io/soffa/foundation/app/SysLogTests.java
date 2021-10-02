package io.soffa.foundation.app;

import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.core.ApiHeaders;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.test.HttpExpect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {"app.sys-logs.enabled=true"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SysLogTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SysLogRepository sysLogs;

    @Test
    public void testSysAction() {
        HttpExpect test = new HttpExpect(mvc);
        TenantHolder.set("T1");
        long initialCount = sysLogs.count();
        test.get("/ping").expect().isOK().contentIs("PONG");
        assertEquals(initialCount + 1, sysLogs.count());

        test.get("/ping").
            header(ApiHeaders.TENANT_ID, "T1").
            header(ApiHeaders.APPLICATION, "Demo").
            expect().isOK().contentIs("PONG");
        assertEquals(initialCount + 2, sysLogs.count());
    }


}
