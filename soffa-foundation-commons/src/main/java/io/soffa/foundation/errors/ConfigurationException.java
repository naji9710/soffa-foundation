package io.soffa.foundation.errors;

import io.soffa.foundation.commons.TextUtil;

public class ConfigurationException extends RuntimeException implements ManagedException {

    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message, Object... args) {
        super(TextUtil.format(message, args));
    }

    public ConfigurationException(Throwable cause, String message, Object... args) {
        super(TextUtil.format(message, args), cause);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
