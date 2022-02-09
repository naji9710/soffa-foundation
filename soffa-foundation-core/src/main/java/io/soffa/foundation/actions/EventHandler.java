package io.soffa.foundation.actions;

import io.soffa.foundation.events.Event;

import java.util.Optional;

public interface EventHandler {

    /**
     * Dispatch event to the right action handler base on the event name
     *
     * @param event The event to handle
     */
    Optional<Object> handle(Event event);
}
