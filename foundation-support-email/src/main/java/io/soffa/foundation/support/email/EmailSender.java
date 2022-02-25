package io.soffa.foundation.support.email;

import io.soffa.foundation.support.email.model.Email;
import io.soffa.foundation.support.email.model.EmailAck;

public interface EmailSender {

    EmailAck send(Email message);

}
