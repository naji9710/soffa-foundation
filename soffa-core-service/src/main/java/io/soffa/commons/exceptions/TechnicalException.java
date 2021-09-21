package io.soffa.commons.exceptions;

import java.text.MessageFormat;

public class TechnicalException extends RuntimeException {

    public TechnicalException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public TechnicalException(Throwable cause, String message, Object... args) {
        super(MessageFormat.format(message, args), cause);
    }

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
