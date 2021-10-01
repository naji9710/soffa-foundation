package app;

import app.config.TestPubSubListener;
import io.soffa.foundation.pubsub.Event;
import io.soffa.foundation.pubsub.PubSubClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class RabbitMQTest {

    @Autowired
    private PubSubClient pubSubClient;

    @SneakyThrows
    @Test
    public void testRabbitMQ() {
        Assertions.assertNotNull(pubSubClient);
        pubSubClient.sendInternal(new Event("HELLO1"));
        pubSubClient.broadcast(new Event("HELLO2"));
        assertTrue(TestPubSubListener.LATCH.await(5, TimeUnit.SECONDS));
        assertEquals(0, TestPubSubListener.LATCH.getCount());
    }

}
