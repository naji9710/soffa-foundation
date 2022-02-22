package io.soffa.foundation.service.pubsub;

import io.soffa.foundation.commons.EventBus;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.core.pubsub.MessageHandler;
import io.soffa.foundation.core.pubsub.PubSubClient;
import io.soffa.foundation.core.pubsub.PubSubMessenger;
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
        } else {
            defaultClient = clients.values().iterator().next();
        }
        EventBus.register(this);
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
    public void setDefaultBroadcast(String value) {
        getDefaultClient().setDefaultBroadcast(value);
    }

    /*
    @Subscribe
    public void onDatabaseReady(DatabaseReadyEvent ignore) {
        EventBus.unregister(this);
        for (Map.Entry<String, PubSubClientConfig> e : config.getClients().entrySet()) {
            String subjects = e.getValue().getSubjects();
            configureListeners(clients.get(e.getKey()), subjects);
        }
        PubSubReadiness.setReady();
    }

    private void configureListeners(PubSubClient client, String subjects) {
        if (TextUtil.isEmpty(subjects)) {
            return;
        }
        String[] subs = subjects.split(",");
        for (String sub : subs) {
            if (TextUtil.isNotEmpty(sub)) {
                boolean isBroadcast = sub.endsWith("*");
                String rsub = sub.replaceAll("\\*", "");
                if (isBroadcast) {
                    client.setDefaultBroadcast(rsub);
                }
                client.subscribe(rsub, isBroadcast, messageHandler);
            }
        }
    }

     */

}
