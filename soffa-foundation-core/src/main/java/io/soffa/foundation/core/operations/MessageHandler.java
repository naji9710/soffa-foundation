package io.soffa.foundation.core.operations;

import io.soffa.foundation.core.messages.Message;

import java.util.Optional;

public interface MessageHandler {

    Optional<Object> onMessage(Message message);

    default boolean accept(String operation) {
        return true;
    }

}
