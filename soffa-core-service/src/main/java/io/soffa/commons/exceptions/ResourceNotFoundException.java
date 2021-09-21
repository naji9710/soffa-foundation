

package io.soffa.commons.exceptions;

import java.text.MessageFormat;

public class ResourceNotFoundException extends FunctionalException {

    public ResourceNotFoundException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public ResourceNotFoundException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }
}
