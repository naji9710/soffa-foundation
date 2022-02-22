package io.soffa.foundation.pubsub;

import io.soffa.foundation.core.context.DefaultRequestContext;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.core.pubsub.PubSubClient;
import io.soffa.foundation.service.pubsub.simple.SimplePubSubClient;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplePubSubClientTest {

    @Test
    void testClient() {

        PubSubClient client = new SimplePubSubClient();
        AtomicLong counter = new AtomicLong(0);
        client.subscribe("test", false, message -> {
            counter.incrementAndGet();
            return Optional.empty();
        });

        client.publish("test", new Message( "hello", new DefaultRequestContext()));

        assertEquals(1, counter.get());
    }

}
