

package io.soffa.foundation.commons.exceptions;

import java.text.MessageFormat;

public class ResourceNotFoundException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public ResourceNotFoundException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
