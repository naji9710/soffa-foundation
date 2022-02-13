package io.soffa.foundation.messages;

import java.util.concurrent.CompletableFuture;

public class AbstractBinaryClient implements BinaryClient {

    @Override
    public void subsribe(String subject, String queue, MessageHandler handler) {
        // Empty on purpose
    }

    @Override
    public CompletableFuture<byte[]> request(String subject, Message event) {
        return null;
    }

    @Override
    public <T> CompletableFuture<T> request(String subject, Message event, Class<T> expectedClass) {
        return null;
    }

    @Override
    public void publish(String subject, Message message) {
        // Empty on purpose
    }

    @Override
    public void broadcast(Message message) {
        // Empty on purpose
    }
}
