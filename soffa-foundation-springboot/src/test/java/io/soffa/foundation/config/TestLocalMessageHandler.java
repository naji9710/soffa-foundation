package io.soffa.foundation.config;

import io.soffa.foundation.core.actions.MessageHandler;
import io.soffa.foundation.core.messages.Message;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class TestLocalMessageHandler implements MessageHandler {

    public static final AtomicInteger TICK = new AtomicInteger(0);
    private static final String HELLO = "HELLO";

    @Override
    public Optional<Object> onMessage(Message message) {
        TICK.incrementAndGet();
        return Optional.empty();
    }

    @Override
    public boolean accept(String action) {
        return HELLO.equals(action);
    }
}
