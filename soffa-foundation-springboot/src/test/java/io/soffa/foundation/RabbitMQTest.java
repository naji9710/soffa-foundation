package io.soffa.foundation;

import io.soffa.foundation.config.TestAmqpListener;
import io.soffa.foundation.core.messages.AmqpClient;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.exceptions.TechnicalException;
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
    "app.amqp.clients.t1=amqp://guest:guest@localhost:5672",
})
public class RabbitMQTest {

    @Autowired
    private AmqpClient amqpClient;

    @SneakyThrows
    @Test
    public void testRabbitMQ() {
        Assertions.assertNotNull(amqpClient);
        amqpClient.sendInternal(new Message("HELLO"));
        amqpClient.sendInternal(new Message("HELLO1"));
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
            return 1 == TestAmqpListener.TICK.intValue();
        });
        Assertions.assertThrowsExactly(TechnicalException.class, () -> {
            amqpClient.send("t1", "exchange1", "routing1", new Message("HELLO2"));
        });

    }

}
