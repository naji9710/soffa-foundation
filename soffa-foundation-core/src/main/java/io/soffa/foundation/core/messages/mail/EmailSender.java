package io.soffa.foundation.core.messages.mail;

import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailId;

public interface EmailSender {

    EmailId send(Email message);

}
