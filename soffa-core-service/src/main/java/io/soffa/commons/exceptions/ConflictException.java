

package io.soffa.commons.exceptions;

import java.text.MessageFormat;

public class ConflictException extends FunctionalException {

    public ConflictException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public ConflictException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
