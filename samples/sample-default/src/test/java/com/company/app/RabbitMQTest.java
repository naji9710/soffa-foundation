package com.company.app;

import io.soffa.foundation.messages.Message;
import io.soffa.foundation.messages.PubSubClient;
import io.soffa.foundation.metrics.MetricsRegistry;
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
    "app.amqp.clients.default.addresses=embedded",
    "app.amqp.clients.t1.addresses=amqp://guest:guest@localhost:5673",
    "AMQP_USERNAME=guest",
    "AMQP_PASSWORD=guest",
})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RabbitMQTest {

    @Autowired
    private PubSubClient pubSub;

    @Autowired
    private MetricsRegistry metricsRegistry;

    private double countEcho() {
        return metricsRegistry.globalCounter("app_operation_echo");
    }

    @SneakyThrows
    @Test
    public void testRabbitMQ() {
        Assertions.assertNotNull(pubSub);

        double echoCounter = countEcho();

        pubSub.publish(new Message("Echo", "Hello World!"));
        pubSub.broadcast(new Message("Echo", "Hello World!"));

        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            //EL
            double value = countEcho();
            return echoCounter + 2 == value;
        });
    }

}
