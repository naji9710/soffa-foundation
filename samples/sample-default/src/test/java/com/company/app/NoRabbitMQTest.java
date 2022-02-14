package com.company.app;

import io.soffa.foundation.messages.PubSubClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(properties = {"app.amqp.enabled=false"})
public class NoRabbitMQTest {

    @Autowired(required = false)
    private PubSubClient client;

    @SneakyThrows
    @Test
    public void testNoRabbitMQ() {
        Assertions.assertNull(client);
    }

}
