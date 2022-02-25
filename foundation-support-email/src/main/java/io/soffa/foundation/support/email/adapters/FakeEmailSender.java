package io.soffa.foundation.support.email.adapters;

import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.RandomUtil;
import io.soffa.foundation.support.email.EmailSender;
import io.soffa.foundation.support.email.model.Email;
import io.soffa.foundation.support.email.model.EmailAck;

import java.util.concurrent.atomic.AtomicInteger;

public class FakeEmailSender implements EmailSender {

    private static final Logger LOG = Logger.get(FakeEmailSender.class);
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Override
    public EmailAck send(Email message) {
        LOG.info("Email processed by FakeEmailSender:\nFrom: %s\nSubject: %s\nTo: %s", message.getSender(), message.getSubject(), JsonUtil.serialize(message.getTo()));
        COUNTER.incrementAndGet();
        return new EmailAck("OK", RandomUtil.nextString());
    }

    public static int getCounter() {
        return COUNTER.get();
    }
}
