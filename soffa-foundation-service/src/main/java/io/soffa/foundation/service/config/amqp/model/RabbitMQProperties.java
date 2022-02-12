package io.soffa.foundation.service.config.amqp.model;

import lombok.Data;

import java.util.Map;

@Data
public class RabbitMQProperties {

    private Map<String, String> clients;
}
