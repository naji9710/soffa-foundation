package io.soffa.foundation.app.events;

import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.core.ApiHeaders;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.test.HttpExpect;
import io.soffa.foundation.test.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


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
        TenantHolder.use("T1", (tenantId) -> {
            HttpExpect test = new HttpExpect(mvc);

            long initialCount = sysLogs.count();
            test.get("/ping").withTenant(tenantId.getValue()).expect().isOK().json("$.value", "PONG");

            TestUtil.awaitUntil(3, () -> {
                TenantHolder.set(tenantId);
                return initialCount + 1 == sysLogs.count();
            });

            test.get("/ping").
                withTenant(tenantId.getValue()).
                header(ApiHeaders.APPLICATION, "Demo").
                expect().isOK().json("$.value", "PONG");

            TestUtil.awaitUntil(3, () -> {
                TenantHolder.set(tenantId);
                return initialCount + 2 == sysLogs.count();
            });
        });

    }


}
