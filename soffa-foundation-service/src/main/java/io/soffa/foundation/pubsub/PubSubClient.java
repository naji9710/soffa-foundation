package io.soffa.foundation.pubsub;


public interface PubSubClient {

    void send(String channel, Event event);

    void broadcast(Event event);

    void sendInternal(Event event);

}
