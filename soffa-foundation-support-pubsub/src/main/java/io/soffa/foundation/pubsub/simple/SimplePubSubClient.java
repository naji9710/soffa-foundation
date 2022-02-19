package io.soffa.foundation.pubsub.simple;

import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.errors.ConfigurationException;
import io.soffa.foundation.model.Message;
import io.soffa.foundation.model.OperationResult;
import io.soffa.foundation.pubsub.AbstractPubSubClient;
import io.soffa.foundation.pubsub.MessageHandler;
import io.soffa.foundation.pubsub.PubSubClient;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SimplePubSubClient extends AbstractPubSubClient implements PubSubClient {

    private final Map<String, MessageHandler> subscriptions = new ConcurrentHashMap<>();


    public SimplePubSubClient() {
        super(null, null, null);
    }

    @Override
    public void subscribe(@NonNull String subject, boolean broadcast, MessageHandler messageHandler) {
        subscriptions.putIfAbsent(subject, messageHandler);
    }

    @Override
    public CompletableFuture<byte[]> internalRequest(@NonNull String subject, Message message) {
        checkSubject(subject);
        return CompletableFuture.supplyAsync(() -> {
            Object result = subscriptions.get(subject).handle(message).orElse(null);
            OperationResult opr = OperationResult.create(ObjectUtil.serialize(result), null);
            return ObjectUtil.serialize(opr);
        });
    }

    @Override
    public void publish(@NonNull String subject, Message message) {
        checkSubject(subject);
        subscriptions.get(subject).handle(message);
    }

    @Override
    public void broadcast(@NonNull String target, Message message) {
        if ("*".equals(target)) {
            new HashSet<>(subscriptions.values()).forEach(handler -> handler.handle(message));
            return;
        }
        checkSubject(target);
        subscriptions.get(target).handle(message);
    }

    @Override
    public void setDefaultBroadcast(String value) {
        // no-op
    }

    private void checkSubject(String target) {
        if (!subscriptions.containsKey(target)) {
            throw new ConfigurationException("Unregistered subject: %s", target);
        }
    }
}
