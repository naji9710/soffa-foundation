package com.company.app.operations;

import io.soffa.foundation.core.Operation;
import io.soffa.foundation.core.email.model.Email;
import io.soffa.foundation.core.email.model.EmailId;

public interface SendEmail extends Operation<Email, EmailId> {
}
