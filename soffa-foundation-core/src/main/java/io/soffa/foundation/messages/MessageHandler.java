package io.soffa.foundation.messages;


import java.util.Optional;

public interface MessageHandler {

    Optional<Object> onMessage(Message message);

    default boolean accept(String operation) {
        return true;
    }

}
