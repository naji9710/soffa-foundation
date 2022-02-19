package io.soffa.foundation.pubsub;


import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.errors.ConfigurationException;
import io.soffa.foundation.errors.NotImplementedException;
import io.soffa.foundation.errors.TodoException;
import io.soffa.foundation.pubsub.config.PubSubClientConfig;
import io.soffa.foundation.pubsub.config.PubSubConfig;
import io.soffa.foundation.pubsub.nats.NatsClient;
import io.soffa.foundation.pubsub.simple.SimplePubSubClient;

import java.util.HashMap;
import java.util.Map;

public final class PubSubMessengerFactory {

    private static final Logger LOG = Logger.get(PubSubMessengerFactory.class);

    private PubSubMessengerFactory() {
    }

    public static PubSubMessenger create(String applicationName, PubSubConfig config, MessageHandler handler) {
        if (config.getClients() == null || config.getClients().isEmpty()) {
            throw new ConfigurationException("No pubsub clients configured");
        }
        Map<String, PubSubClient> clients = new HashMap<>();
        for (Map.Entry<String, PubSubClientConfig> e : config.getClients().entrySet()) {
            PubSubClient client = createClient(applicationName, e.getValue(), config.getBroadcasting());
            String subjects = e.getValue().getSubjects();
            if (TextUtil.isNotEmpty(subjects)) {
                if (handler == null) {
                    throw new ConfigurationException("A MessageHandler is required when  pubsub.subjects is set");
                }
                configureListeners(client, subjects, handler);
            }
            clients.put(e.getKey(), client);
        }
        return new PubSubMessengerImpl(clients);
    }

    private static PubSubClient createClient(String applicationName, PubSubClientConfig config, String broadcasting) {
        config.afterPropertiesSet();
        PubSubClient client;
        if (config.getAddresses().contains("nats://")) {
            LOG.info("Creating NATS client for @%s", config.getAddresses());
            client = new NatsClient(applicationName, config, broadcasting);
        } else if ("simple".equalsIgnoreCase(config.getAddresses())) {
            client = new SimplePubSubClient();
        } else if (config.getAddresses().contains("amqp://")) {
            LOG.info("Creating AMQP client for @%s", config.getAddresses());
            throw new TodoException();
        } else {
            throw new NotImplementedException("PubSubClient not supported " + config.getAddresses());
        }

        return client;
    }

    private static void configureListeners(PubSubClient client, String subjects, MessageHandler handler) {
        if (TextUtil.isEmpty(subjects)) {
            return;
        }
        String[] subs = subjects.split(",");
        for (String sub : subs) {
            LOG.info("Adding listener: %s", sub);
            if (TextUtil.isNotEmpty(sub)) {
                boolean isBroadcast = sub.endsWith("*");
                String rsub = sub.replaceAll("\\*", "");
                if (isBroadcast) {
                    client.setDefaultBroadcast(rsub);
                }
                client.subscribe(rsub, isBroadcast, handler);
            }
        }
    }

}
