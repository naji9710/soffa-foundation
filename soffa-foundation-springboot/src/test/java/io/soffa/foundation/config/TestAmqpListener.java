package io.soffa.foundation.config;

import io.soffa.foundation.core.messages.AmqpListener;
import io.soffa.foundation.core.messages.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;


@Component
public class TestAmqpListener implements AmqpListener {

    public static final AtomicInteger TICK = new AtomicInteger(0);
    private static final String HELLO = "HELLO";

    @Override
    public void handle(Message event) {
        if (HELLO.equals(event.getAction())) {
            TICK.incrementAndGet();
        }
    }
}
