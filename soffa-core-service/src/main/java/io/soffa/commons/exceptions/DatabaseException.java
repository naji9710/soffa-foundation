package io.soffa.commons.exceptions;

public class DatabaseException extends TechnicalException {

    public DatabaseException(Throwable cause, String messsage, Object... args) {
        super(cause, messsage, args);
    }
    public DatabaseException(String messsage, Object... args) {
        super(messsage, args);
    }
}
