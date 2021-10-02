package io.soffa.foundation;

import io.soffa.foundation.core.model.TenantId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseTests {

    @Test
    public void testTenantId() {
        TenantId t = new TenantId("T1");
        assertEquals("T1", t.getValue());
        assertEquals(new TenantId("T1"), t);
    }

}
