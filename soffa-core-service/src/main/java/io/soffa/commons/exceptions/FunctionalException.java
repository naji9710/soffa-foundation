package io.soffa.commons.exceptions;

import org.apache.logging.log4j.message.Message;

import java.text.MessageFormat;

public class FunctionalException extends RuntimeException {

    public FunctionalException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public FunctionalException(Throwable cause, String message, Object... args) {
        super(MessageFormat.format(message, args), cause);
    }
}
