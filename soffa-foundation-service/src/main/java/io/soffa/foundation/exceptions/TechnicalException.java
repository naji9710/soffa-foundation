package io.soffa.foundation.exceptions;

import io.soffa.foundation.lang.TextUtil;

public class TechnicalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TechnicalException(String message, Object... args) {
        super(TextUtil.format(message, args));
    }

    public TechnicalException(Throwable cause, String message, Object... args) {
        super(TextUtil.format(message, args), cause);
    }

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

}
