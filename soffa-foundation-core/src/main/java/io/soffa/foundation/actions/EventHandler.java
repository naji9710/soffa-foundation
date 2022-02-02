package io.soffa.foundation.actions;

import io.soffa.foundation.events.Event;

public interface EventHandler {

    /**
     * Dispatch event to the right action handler base on the event name
     *
     * @param event The event to handle
     */
    void handle(Event event);
}
