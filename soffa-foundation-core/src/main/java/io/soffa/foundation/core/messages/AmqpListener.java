package io.soffa.foundation.core.messages;

public interface AmqpListener {

    default boolean accept(String action) {
        return true;
    }

    void handle(Message event);

}
