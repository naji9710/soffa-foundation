package com.company.app;

import io.soffa.foundation.events.EventDispatcher;
import io.soffa.foundation.pubsub.PubSubClient;
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
    private PubSubClient pubSubClient;

    @Autowired(required = false)
    private EventDispatcher eventDispatcher;

    @SneakyThrows
    @Test
    public void testNoRabbitMQ() {
        Assertions.assertNull(pubSubClient);
        Assertions.assertNull(eventDispatcher);
    }

}
