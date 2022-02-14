package io.soffa.foundation.service.pubsub.nats;

import lombok.Data;

@Data
public class NatsClientConfig {

    private String addresses;
    private String broadcast;
}
