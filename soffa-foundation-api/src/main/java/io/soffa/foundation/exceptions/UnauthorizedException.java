package io.soffa.foundation.exceptions;

public class UnauthorizedException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message, Object... args) {
        super(message, args);
    }

    public UnauthorizedException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

}
