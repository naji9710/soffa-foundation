package io.soffa.foundation.pubsub;

import io.soffa.foundation.events.Event;

public interface PubSubListener {

    void handle(Event event);

}
