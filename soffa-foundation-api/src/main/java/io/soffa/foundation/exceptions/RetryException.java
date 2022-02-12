package io.soffa.foundation.exceptions;

public class RetryException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public RetryException(String message, Object... args) {
        super(message, args);
    }

    public RetryException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
