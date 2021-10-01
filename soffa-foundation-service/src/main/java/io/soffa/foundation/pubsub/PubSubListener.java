package io.soffa.foundation.pubsub;

public interface PubSubListener {

    void handle(Event event);

}
