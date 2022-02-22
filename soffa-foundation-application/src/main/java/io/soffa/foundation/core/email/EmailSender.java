package io.soffa.foundation.core.email;

import io.soffa.foundation.core.email.model.Email;
import io.soffa.foundation.core.email.model.EmailId;

public interface EmailSender {

    EmailId send(Email message);

}
