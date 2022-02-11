package com.company.app.operations;

import io.soffa.foundation.core.operations.Operation;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailId;

public interface SendEmail extends Operation<Email, EmailId> {
}
