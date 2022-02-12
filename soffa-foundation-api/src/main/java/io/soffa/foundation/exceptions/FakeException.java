package io.soffa.foundation.exceptions;

import io.soffa.foundation.commons.TextUtil;

public class FakeException extends RuntimeException implements ManagedException {

    private static final long serialVersionUID = 1L;

    public FakeException(String message, Object... args) {
        super(TextUtil.format(message, args));
    }

    public FakeException(Throwable cause, String message, Object... args) {
        super(TextUtil.format(message, args), cause);
    }
}
