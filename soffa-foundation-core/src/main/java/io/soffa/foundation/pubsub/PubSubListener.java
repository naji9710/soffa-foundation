package io.soffa.foundation.pubsub;

import io.soffa.foundation.events.Event;

public interface PubSubListener {

    default boolean accept(String action) {
        return true;
    }

    void handle(Event event);

}
