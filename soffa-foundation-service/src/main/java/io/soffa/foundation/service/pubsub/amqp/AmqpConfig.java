package io.soffa.foundation.service.pubsub.amqp;

import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.exceptions.ConfigurationException;
import io.soffa.foundation.messages.PubSubClient;
import lombok.Data;

import java.util.Map;

@Data
public class AmqpConfig {

    private Map<String, AmqpClientConfig> clients;

    public AmqpClientConfig getClient(String id) {
        if (TextUtil.isEmpty(id)) {
            return getClient(PubSubClient.DEFAULT_ID);
        }
        if (!clients.containsKey(id)) {
            throw new ConfigurationException("No client with id '" + id + "' found");
        }
        return clients.get(id);
    }

}
