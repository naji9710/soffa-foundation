
package io.soffa.foundation.commons.exceptions;

import java.text.MessageFormat;

public class UnauthorizedException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public UnauthorizedException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
