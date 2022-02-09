package io.soffa.foundation.core.actions;

import io.soffa.foundation.core.messages.Message;

import java.util.Optional;

public interface MessageHandler {

    /**
     * Dispatch event to the right action handler base on the event name
     *
     * @param message The message to handle
     */
    Optional<Object> onMessage(Message message);

    default boolean accept(String action) {
        return true;
    }



}
