package io.soffa.foundation.support.email;

import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailId;

public interface EmailSender {

    EmailId send(Email message);

}
