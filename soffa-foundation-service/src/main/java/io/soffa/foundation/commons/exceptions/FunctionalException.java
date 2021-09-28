package io.soffa.foundation.commons.exceptions;

import java.text.MessageFormat;

public class FunctionalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FunctionalException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public FunctionalException(Throwable cause, String message, Object... args) {
        super(MessageFormat.format(message, args), cause);
    }
}
