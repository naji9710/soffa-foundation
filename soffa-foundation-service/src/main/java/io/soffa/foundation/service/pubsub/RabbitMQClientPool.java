package io.soffa.foundation.service.pubsub;

import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.commons.UrlInfo;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.service.pubsub.amqp.AmqpClientConfig;
import io.soffa.foundation.service.pubsub.amqp.AmqpConfig;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RabbitMQClientPool {

    private final Map<String, RabbitTemplate> templates = new HashMap<>();

    public RabbitMQClientPool(AmqpConfig config) {
        if (config != null && config.getClients() != null) {
            for (Map.Entry<String, AmqpClientConfig> entry : config.getClients().entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
        }
    }

    public boolean hasClient(String id) {
        return templates.containsKey(id.toLowerCase());
    }

    public RabbitTemplate getTemplate(String id) {
        return templates.get(id.toLowerCase());
    }

    private void add(String name, AmqpClientConfig config) {
        String key = name.toLowerCase();
        if (templates.containsKey(key)) {
            throw new TechnicalException("AMQP link already configured: " + key);
        }

        RabbitMQConfig.embeddedMode = false;
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        Set<String> addresses = new HashSet<>();
        for (String address : config.getAddresses().split(",")) {
            UrlInfo url = UrlInfo.parse(address.trim());
            addresses.add(url.getHostnameWithPort());
        }
        String addressList = String.join(",", addresses);
        connectionFactory.setAddresses(addressList);
        connectionFactory.setUsername(config.getUsername());
        connectionFactory.setPassword(config.getPassword());
        String vhost = config.getVhost();
        if (TextUtil.isEmpty(vhost)) {
            vhost = "/";
        }
        connectionFactory.setVirtualHost(vhost);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(config.getExchange());
        rabbitTemplate.setRoutingKey(config.getRouting());
        templates.put(key, rabbitTemplate);
    }

    public Map<String, RabbitTemplate> getTemplates() {
        return templates;
    }


}
