package io.soffa.foundation.messages;

import java.util.concurrent.CompletableFuture;

public class AbstractPubSubClient implements PubSubClient {

    @Override
    public <T> CompletableFuture<T> request(String subject, Message event, Class<T> expectedClass, String client) {
        return null;
    }

    @Override
    public void publish(String subject, Message message, String client) {
        // No implementation
    }

    @Override
    public void broadcast(Message message, String client) {
        // No implementation
    }
}
