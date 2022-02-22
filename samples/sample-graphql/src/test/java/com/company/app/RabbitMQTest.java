package com.company.app;

import io.soffa.foundation.application.messages.AmqpClient;
import io.soffa.foundation.application.messages.Message;
import io.soffa.foundation.application.messages.MessageDispatcher;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

    @SneakyThrows
    @Test
    public void testRabbitMQ() {
        Assertions.assertNotNull(messageDispatcher);
        Assertions.assertNotNull(amqpClient);
        amqpClient.sendInternal(new Message("HELLO1"));
        amqpClient.broadcast(new Message("HELLO2"));
        LocalMessagehandler.LATCH.await();
        assertEquals(0, LocalMessagehandler.LATCH.getCount());
    }

}
