package com.company.app;

import io.soffa.foundation.events.Event;
import io.soffa.foundation.events.EventDispatcher;
import io.soffa.foundation.pubsub.PubSubClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles({"test", "foundation-amqp"})
@SpringBootTest(properties = {
    "app.amqp.addresses=embedded",
    "app.amqp.clients.default=amqp://guest:guest@localhost:5673",
})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RabbitMQTest {

    @Autowired
    private PubSubClient pubSubClient;

    @Autowired
    private EventDispatcher eventDispatcher;

    @SneakyThrows
    @Test
    public void testRabbitMQ() {
        Assertions.assertNotNull(eventDispatcher);
        Assertions.assertNotNull(pubSubClient);
        pubSubClient.sendInternal(new Event("HELLO1"));
        pubSubClient.broadcast(new Event("HELLO2"));
        TestPubSubListener.LATCH.await();
        assertEquals(0, TestPubSubListener.LATCH.getCount());
    }

}
