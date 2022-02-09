package io.soffa.foundation.pubsub;

import io.soffa.foundation.events.Event;

import java.util.Optional;

public interface BinaryMessageHandler {

    Optional<Object> onMessage(Event msg)  ;

}
