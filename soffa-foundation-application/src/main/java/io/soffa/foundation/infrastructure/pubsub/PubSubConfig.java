package io.soffa.foundation.infrastructure.pubsub;

import lombok.Data;

import java.util.Map;

@Data
public class PubSubConfig {

    private String broadcasting;
    private Map<String, PubSubClientConfig> clients;

}
