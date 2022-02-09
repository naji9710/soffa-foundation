package com.company.app;

import io.soffa.foundation.core.messages.AmqpClient;
import io.soffa.foundation.core.messages.MessageDispatcher;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class NoRabbitMQTest {

    @Autowired(required = false)
    private AmqpClient amqpClient;

    @Autowired(required = false)
    private MessageDispatcher messageDispatcher;

    @SneakyThrows
    @Test
    public void testNoRabbitMQ() {
        Assertions.assertNull(amqpClient);
        Assertions.assertNull(messageDispatcher);
    }

}
