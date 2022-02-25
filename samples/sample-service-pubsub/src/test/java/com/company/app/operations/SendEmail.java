package com.company.app.operations;

import io.soffa.foundation.support.email.model.Email;
import io.soffa.foundation.support.email.model.EmailAck;
import io.soffa.foundation.core.Operation;

public interface SendEmail extends Operation<Email, EmailAck> {
}
