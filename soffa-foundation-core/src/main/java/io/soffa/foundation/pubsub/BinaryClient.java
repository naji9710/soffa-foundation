package io.soffa.foundation.pubsub;


import io.soffa.foundation.events.Event;

import java.util.concurrent.CompletableFuture;

public interface BinaryClient {

    void subsribe(String subject, BinaryMessageHandler handler);

    CompletableFuture<byte[]> request(String subject, Event event);

    <T> CompletableFuture<T> request(String subject, Event event, Class<T> expectedClass);

    void publish(String subject, Event message);


}
