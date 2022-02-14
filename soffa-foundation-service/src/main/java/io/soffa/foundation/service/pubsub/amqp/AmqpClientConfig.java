package io.soffa.foundation.service.pubsub.amqp;

import lombok.Data;

@Data
public class AmqpClientConfig {

    private String exchange;
    private String routing;
    private String vhost = "/";
    private String addresses;
    private String username;
    private String password;
}
