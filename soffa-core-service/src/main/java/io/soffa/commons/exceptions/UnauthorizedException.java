
package io.soffa.commons.exceptions;

import java.text.MessageFormat;

public class UnauthorizedException extends FunctionalException {

    public UnauthorizedException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public UnauthorizedException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
