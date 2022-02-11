package com.company.app.operations;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class SendEmailHandler implements SendEmail {

    public static final AtomicLong COUNTER = new AtomicLong(0);

    @Override
    public EmailId handle(@NonNull Email input, @NonNull RequestContext context) {
        COUNTER.incrementAndGet();
        return new EmailId("000");
    }
}
