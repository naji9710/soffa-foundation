
package io.soffa.foundation.commons.exceptions;

import java.text.MessageFormat;

public class ValidationException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public ValidationException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
