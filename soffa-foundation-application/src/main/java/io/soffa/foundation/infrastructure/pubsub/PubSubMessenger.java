package io.soffa.foundation.infrastructure.pubsub;

public interface PubSubMessenger extends PubSubClient {

    PubSubClient getDefaultClient();

    PubSubClient getClient(String name);

    default void afterPropertiesSet() {
        // Implements if needed
    }

}
