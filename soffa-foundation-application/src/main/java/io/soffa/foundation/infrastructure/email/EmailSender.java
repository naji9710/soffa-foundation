package io.soffa.foundation.infrastructure.email;

import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailId;

public interface EmailSender {

    EmailId send(Email message);

}
