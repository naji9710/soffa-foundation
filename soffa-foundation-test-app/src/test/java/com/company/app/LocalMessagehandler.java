package com.company.app;

import io.soffa.foundation.core.actions.MessageHandler;
import io.soffa.foundation.core.messages.Message;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;


@Component
public class LocalMessagehandler implements MessageHandler {

    public static final CountDownLatch LATCH = new CountDownLatch(2);
    private static final String HELLO = "HELLO";

    @Override
    public Optional<Object> onMessage(Message message) {
        LATCH.countDown();
        return Optional.empty();
    }

    @Override
    public boolean accept(String action) {
        return action.startsWith(HELLO);
    }
}
