package io.soffa.foundation.config;

import io.soffa.foundation.pubsub.Event;
import io.soffa.foundation.pubsub.PubSubListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;


@Component
public  class TestPubSubListener implements PubSubListener {

    public static final CountDownLatch LATCH = new CountDownLatch(2);
    private static final String HELLO = "HELLO";

    @Override
    public void handle(Event event) {
        if (event.getAction().startsWith(HELLO)) {
            LATCH.countDown();
        }
    }
}
