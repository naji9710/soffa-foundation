package com.company.app.operations;

import io.soffa.foundation.support.email.model.Email;
import io.soffa.foundation.support.email.model.EmailAck;
import io.soffa.foundation.core.RequestContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class SendEmailHandler implements SendEmail {

    public static final AtomicLong COUNTER = new AtomicLong(0);

    @Override
    public EmailAck handle(@NonNull Email input, @NonNull RequestContext context) {
        COUNTER.incrementAndGet();
        return new EmailAck("OK", "000");
    }
}
