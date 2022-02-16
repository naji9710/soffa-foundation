package io.soffa.foundation.pubsub;

import io.soffa.foundation.api.Operation;
import io.soffa.foundation.model.Message;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PubSubMessengerImpl implements PubSubMessenger {

    private final Map<String, PubSubClient> clients;
    private final PubSubClient defaultClient;
    public static final String DEFAULT = "default";

    public PubSubMessengerImpl(Map<String, PubSubClient> clients) {
        this.clients = clients;
        if (clients.containsKey(DEFAULT)) {
            defaultClient = clients.get(DEFAULT);
        }else {
            defaultClient = clients.values().iterator().next();
        }
    }

    @Override
    public PubSubClient getDefaultClient() {
        return defaultClient;
    }

    @Override
    public PubSubClient getClient(String name) {
        return clients.get(name);
    }

    @Override
    public void subscribe(@NonNull String subject, boolean broadcast, MessageHandler messageHandler) {
        getDefaultClient().subscribe(subject, broadcast, messageHandler);
    }

    @Override
    public <T> CompletableFuture<T> request(@NonNull String subject, @NotNull Message message, Class<T> expectedClass) {
        return getDefaultClient().request(subject, message, expectedClass);
    }

    @Override
    public void publish(@NonNull String subject, @NotNull Message message) {
        getDefaultClient().publish(subject, message);
    }

    @Override
    public void broadcast(@NonNull String target, @NotNull Message message) {
        getDefaultClient().broadcast(target, message);
    }

    @Override
    public <I, O, T extends Operation<I, O>> T proxy(@NonNull String subjet, Class<T> operationClass) {
        return getDefaultClient().proxy(subjet, operationClass);
    }

    @Override
    public void setDefaultBroadcast(String value) {
        getDefaultClient().setDefaultBroadcast(value);
    }
}
