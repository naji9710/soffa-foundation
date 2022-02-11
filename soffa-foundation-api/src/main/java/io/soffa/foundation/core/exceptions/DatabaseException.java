package io.soffa.foundation.core.exceptions;

public class DatabaseException extends TechnicalException {

    private static final long serialVersionUID = -9219661125423412050L;

    public DatabaseException(Throwable cause, String messsage, Object... args) {
        super(cause, messsage, args);
    }

    public DatabaseException(String messsage, Object... args) {
        super(messsage, args);
    }
}
