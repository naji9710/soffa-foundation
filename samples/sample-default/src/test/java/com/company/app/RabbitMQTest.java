package com.company.app;

import io.soffa.foundation.core.messages.AmqpClient;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.core.messages.MessageDispatcher;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

@ActiveProfiles({"test", "foundation-amqp"})
@SpringBootTest(properties = {
    "app.amqp.addresses=embedded",
    "app.amqp.clients.default=amqp://guest:guest@localhost:5673",
})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RabbitMQTest {

    @Autowired
    private AmqpClient amqpClient;

    @Autowired
    private MessageDispatcher messageDispatcher;

    @Autowired
    private MetricsRegistry metricsRegistry;

    private double countEcho() {
        return metricsRegistry.globalCounter("app_operation_echo");
    }

    @SneakyThrows
    @Test
    public void testRabbitMQ() {
        Assertions.assertNotNull(messageDispatcher);
        Assertions.assertNotNull(amqpClient);

        double echoCounter = countEcho();

        amqpClient.publishSelf(new Message("Echo", "Hello World!"));
        amqpClient.broadcast(new Message("Echo", "Hello World!"));

        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            //EL
            double value = countEcho();
            return echoCounter + 2 == value;
        });
    }

}
