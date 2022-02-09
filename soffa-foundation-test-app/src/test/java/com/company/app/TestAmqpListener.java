package com.company.app;

import io.soffa.foundation.core.messages.AmqpListener;
import io.soffa.foundation.core.messages.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;


@Component
public class TestAmqpListener implements AmqpListener {

    public static final CountDownLatch LATCH = new CountDownLatch(2);
    private static final String HELLO = "HELLO";

    @Override
    public void handle(Message event) {
        if (event.getAction().startsWith(HELLO)) {
            LATCH.countDown();
        }
    }
}
