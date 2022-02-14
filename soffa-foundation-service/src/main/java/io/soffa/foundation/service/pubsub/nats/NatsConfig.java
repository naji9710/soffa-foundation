package io.soffa.foundation.service.pubsub.nats;

import io.soffa.foundation.messages.PubSubClient;
import lombok.Data;

import java.util.Map;

@Data
public class NatsConfig {

    private boolean enabled;
    private Map<String,NatsClientConfig> clients;

    public boolean hasDefaultClient() {
        return clients.containsKey(PubSubClient.DEFAULT_ID);
    }

}
