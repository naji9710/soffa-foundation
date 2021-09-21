
package io.soffa.commons.exceptions;

import java.text.MessageFormat;

public class ValidationException extends FunctionalException {

    public ValidationException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public ValidationException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
