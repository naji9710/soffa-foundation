

package io.soffa.foundation.commons.exceptions;

import java.text.MessageFormat;

public class ConflictException extends FunctionalException {

    private static final long serialVersionUID = -8371753258676604024L;

    public ConflictException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public ConflictException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
