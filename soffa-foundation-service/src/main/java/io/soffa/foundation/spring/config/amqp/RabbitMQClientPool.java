package io.soffa.foundation.spring.config.amqp;

import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.commons.UrlInfo;
import io.soffa.foundation.core.exceptions.TechnicalException;
import io.soffa.foundation.spring.config.amqp.model.RabbitMQProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RabbitMQClientPool {

    private final Map<String, RabbitTemplate> templates = new HashMap<>();

    public RabbitMQClientPool(RabbitMQProperties properties) {
        if (properties!=null && properties.getClients()!=null) {
            for (Map.Entry<String, String> entry : properties.getClients().entrySet()) {
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

    private void add(String name, String amqpurl) {
        String key = name.toLowerCase();
        if (templates.containsKey(key)) {
            throw new TechnicalException("AMQP link already configured: " + key);
        }

        RabbitMQConfig.embeddedMode = false;
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        Set<String> addresses = new HashSet<>();
        UrlInfo firstAddress = null;
        for (String address : amqpurl.split(",")) {
            UrlInfo url = UrlInfo.parse(address.trim());
            addresses.add(url.getHostnameWithPort());
            if (firstAddress == null) {
                firstAddress = url;
            }
        }
        String addressList = String.join(",", addresses);
        connectionFactory.setAddresses(addressList);
        if (firstAddress != null) {
            connectionFactory.setUsername(firstAddress.getUsername());
            connectionFactory.setPassword(firstAddress.getPassword());
            String vhost = firstAddress.getPath();
            if (TextUtil.isEmpty(vhost)) {
                vhost = "/";
            }
            connectionFactory.setVirtualHost(vhost);
        }

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        templates.put(key, rabbitTemplate);
    }

    public Map<String, RabbitTemplate> getTemplates() {
        return templates;
    }


}
