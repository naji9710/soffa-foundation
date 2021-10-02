package io.soffa.foundation.exceptions;

import io.soffa.foundation.lang.TextUtil;

public class FunctionalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FunctionalException(String message, Object... args) {
        super(TextUtil.format(message, args));
    }

    public FunctionalException(Throwable cause, String message, Object... args) {
        super(TextUtil.format(message, args), cause);
    }
}
