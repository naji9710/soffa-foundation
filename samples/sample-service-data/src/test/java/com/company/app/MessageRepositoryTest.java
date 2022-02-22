package com.company.app;

import com.company.app.gateways.MessageRepository;
import io.soffa.foundation.core.context.RequestContextHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messages;

    @Test
    public void testConfig() {
        RequestContextHolder.setTenant("T1");
        assertTrue(messages.count() >= 0);
        RequestContextHolder.setTenant("T2");
        assertTrue(messages.count() >= 0);
        RequestContextHolder.clear();
        assertTrue(messages.count() >= 0);

    }


}
