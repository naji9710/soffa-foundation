

package io.soffa.foundation.commons.exceptions;

import java.text.MessageFormat;

public class ForbiddenException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public ForbiddenException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public ForbiddenException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
