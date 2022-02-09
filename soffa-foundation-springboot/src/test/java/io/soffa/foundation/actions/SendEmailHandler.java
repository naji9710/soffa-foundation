package io.soffa.foundation.actions;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SendEmailHandler implements SendEmail {

    @Override
    public EmailId handle(@NonNull Email input, @NonNull RequestContext context) {
        return new EmailId("000");
    }
}
