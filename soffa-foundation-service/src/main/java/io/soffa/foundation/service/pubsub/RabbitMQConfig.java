package io.soffa.foundation.service.pubsub;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.config.AppConfig;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.messages.Message;
import io.soffa.foundation.messages.PubSubClient;
import io.soffa.foundation.service.pubsub.amqp.AmqpClientConfig;
import io.soffa.foundation.service.pubsub.amqp.AmqpConfig;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Configuration
@ConditionalOnProperty(value = "app.amqp.enabled", havingValue = "true")
public class RabbitMQConfig {

    private static final String DLQ = ".dlq";
    private static final String TOPIC = ".topic";
    private static final String FANOUT = ".fanout";
    private static final Logger LOG = Logger.get(RabbitMQConfig.class);
    static boolean embeddedMode;
    private final RabbitTemplate rabbitTemplate;
    private final String applicationName;
    private final String exchange;
    private final String routing;
    private final AmqpConfig config;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RabbitMQConfig(RabbitTemplate rabbitTemplate,
                          AppConfig appConfig,
                          AmqpConfig config) {
        this.config = config;
        this.rabbitTemplate = rabbitTemplate;
        this.applicationName = appConfig.getName();
        AmqpClientConfig defaultClient = config.getClient(PubSubClient.DEFAULT_ID);
        this.exchange = defaultClient.getExchange();
        this.routing = defaultClient.getRouting();
    }

    @Bean
    Queue queue() {
        Map<String, Object> args = ImmutableMap.of(
            "x-dead-letter-exchange", exchange + DLQ,
            "x-dead-letter-routing-key", routing + "." + applicationName
        );
        return new Queue(applicationName, true, false, false, args);
    }

    @Bean
    Queue deadLetterQueue() {
        return new Queue(applicationName + DLQ);
    }

    @Bean
    TopicExchange createTopicExchange() {
        return new TopicExchange(exchange + TOPIC, true, false);
    }

    @Bean
    TopicExchange createDeadLetterExchange() {
        return new TopicExchange(exchange + DLQ, true, false);
    }

    @Bean
    public FanoutExchange createFanoutExchange() {
        return new FanoutExchange(exchange + FANOUT, true, false);
    }

    @Bean
    Binding createTopicBinding() {
        return BindingBuilder.bind(queue()).to(createTopicExchange()).with(routing + "." + applicationName);
    }

    @Bean
    Binding createDQLBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(createDeadLetterExchange()).with(routing + "." + applicationName);
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    Binding createFanoutBinding(Queue queue, FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Primary
    @Bean
    PubSubClient createPubSubClient(AmqpConfig rabbitMQProperties) {

        LOG.info("Creating AmqpClientConfig");
        RabbitMQClientPool clients = new RabbitMQClientPool(rabbitMQProperties);

        return new PubSubClient() {

            private RabbitTemplate getTemplate(String clientId) {
                if (TextUtil.isEmpty(clientId)) {
                    return getTemplate(PubSubClient.DEFAULT_ID);
                }
                if (PubSubClient.DEFAULT_ID.equalsIgnoreCase(clientId)) {
                    return rabbitTemplate;
                } else if (clients.hasClient(clientId)) {
                    return clients.getTemplate(clientId);
                } else {
                    throw new TechnicalException("AMQP client not found: %s", clientId);
                }
            }

            private String getSubject(String subject) {
                if (TextUtil.isEmpty(subject)) {
                    return applicationName;
                }
                return subject;
            }

            @Override
            public <T> CompletableFuture<T> request(String subject, Message message, Class<T> expectedClass, String client) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[PUB-SUB] Exchanging message with to %s.* - @operation:%s", subject, message.getOperation());
                }
                return CompletableFuture.supplyAsync(() -> {
                    //noinspection unchecked
                    return (T) getTemplate(client).convertSendAndReceive(subject, JsonUtil.serialize(message).getBytes());
                });
            }

            @Override
            public void publish(String subject, Message message, String client) {
                if (TextUtil.isEmpty(message.getId())) {
                    message.setId(IdGenerator.secureRandomId("evt_"));
                }
                String sub = getSubject(subject);
                AmqpClientConfig clientConfig = config.getClient(client);
                try {
                    getTemplate(client).convertAndSend(clientConfig.getExchange() + TOPIC, clientConfig.getRouting() + "." + sub, JsonUtil.serialize(message).getBytes());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[PUB-SUB] Message sent to %s - @operation:%s", sub, message.getOperation());
                    }
                } catch (AmqpException e) {
                    throw new TechnicalException(e, "Error sending amqp message to client:%s @exchange:%s routing:%s operation:%s", client, clientConfig.getExchange(), clientConfig.getRouting(), message.getOperation());
                }
            }

            @Override
            public void broadcast(Message event, String client) {
                if (TextUtil.isNotEmpty(client)) {
                    throw new TechnicalException("Broadcast not supported for client:%s", client);
                }
                AmqpClientConfig clientConfig = config.getClient(client);
                getTemplate(client).convertAndSend(clientConfig.getExchange() + FANOUT, clientConfig.getRouting() + ".*", JsonUtil.serialize(event).getBytes());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[PUB-SUB] Message broadcasted to %s.* - @operation:%s", clientConfig.getRouting(), event.getOperation());
                }
            }

        };
    }


}
