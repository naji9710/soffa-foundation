package com.company.app.actions;

import io.soffa.foundation.core.actions.Action;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailId;

public interface SendEmail extends Action<Email, EmailId> {
}
