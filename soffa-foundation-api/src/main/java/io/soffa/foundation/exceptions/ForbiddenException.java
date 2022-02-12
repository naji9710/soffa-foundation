package io.soffa.foundation.exceptions;

public class ForbiddenException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public ForbiddenException(String message, Object... args) {
        super(message, args);
    }

    public ForbiddenException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
