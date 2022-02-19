package io.soffa.foundation.pubsub;

import com.google.common.eventbus.Subscribe;
import io.soffa.foundation.api.Operation;
import io.soffa.foundation.commons.EventBus;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.data.DatabaseReadyEvent;
import io.soffa.foundation.model.Message;
import io.soffa.foundation.pubsub.config.PubSubClientConfig;
import io.soffa.foundation.pubsub.config.PubSubConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PubSubMessengerImpl implements PubSubMessenger {

    private final PubSubConfig config;
    private final Map<String, PubSubClient> clients;
    private final PubSubClient defaultClient;
    private final MessageHandler messageHandler;
    public static final String DEFAULT = "default";

    public PubSubMessengerImpl(PubSubConfig config, MessageHandler messageHandler, Map<String, PubSubClient> clients) {
        this.config = config;
        this.clients = clients;
        this.messageHandler = messageHandler;
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
    public <I, O, T extends Operation<I, O>> T proxy(@NonNull String subjet, Class<T> operationClass) {
        return getDefaultClient().proxy(subjet, operationClass);
    }

    @Override
    public void setDefaultBroadcast(String value) {
        getDefaultClient().setDefaultBroadcast(value);
    }

    @Subscribe
    public void onDatabaseReady(DatabaseReadyEvent ignore) {
        EventBus.unregister(this);
        for (Map.Entry<String, PubSubClientConfig> e : config.getClients().entrySet()) {
            String subjects = e.getValue().getSubjects();
            configureListeners(clients.get(e.getKey()), subjects);
        }
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

}
