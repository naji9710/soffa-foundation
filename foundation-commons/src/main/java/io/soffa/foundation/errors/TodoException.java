package io.soffa.foundation.errors;

public class TodoException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public TodoException() {
        super("Not implementation yet");
    }

    public TodoException(String messsage) {
        super(messsage);
    }

}
