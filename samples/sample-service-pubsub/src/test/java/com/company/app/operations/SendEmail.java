package com.company.app.operations;

import io.soffa.foundation.core.Operation;
import io.soffa.foundation.support.email.model.Email;
import io.soffa.foundation.support.email.model.EmailAck;

public interface SendEmail extends Operation<Email, EmailAck> {
}
