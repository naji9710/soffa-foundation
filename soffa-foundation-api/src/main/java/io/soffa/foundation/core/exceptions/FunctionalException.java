package io.soffa.foundation.core.exceptions;

import io.soffa.foundation.commons.TextUtil;

public class FunctionalException extends RuntimeException implements ManagedException {

    private static final long serialVersionUID = 1L;

    public FunctionalException(String message, Object... args) {
        super(TextUtil.format(message, args));
    }

    public FunctionalException(Throwable cause, String message, Object... args) {
        super(TextUtil.format(message, args), cause);
    }
}
