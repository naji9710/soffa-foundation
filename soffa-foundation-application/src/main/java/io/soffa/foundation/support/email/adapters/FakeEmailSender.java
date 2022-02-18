package io.soffa.foundation.support.email.adapters;

import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.RandomUtil;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailId;
import io.soffa.foundation.support.email.EmailSender;

import java.util.concurrent.atomic.AtomicInteger;

public class FakeEmailSender implements EmailSender {

    private static final Logger LOG = Logger.get(FakeEmailSender.class);
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Override
    public EmailId send(Email message) {
        LOG.info("Email processed by FakeEmailSender:\nFrom: %s\nSubject: %s\nTo: %s", message.getSender(), message.getSubject(), JsonUtil.serialize(message.getTo()));
        COUNTER.incrementAndGet();
        return new EmailId(RandomUtil.nextString());
    }

    public static int getCounter() {
        return COUNTER.get();
    }
}
