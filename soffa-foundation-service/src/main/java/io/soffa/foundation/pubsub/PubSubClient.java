package io.soffa.foundation.pubsub;


import io.soffa.foundation.events.Event;

public interface PubSubClient {

    void send(String channel, Event event);

    void broadcast(Event event);

    void sendInternal(Event event);

}
