package com.company.app;

import com.company.app.core.Ping;
import com.company.app.core.PingResponse;
import com.company.app.operations.SendEmailHandler;
import io.soffa.foundation.api.Operation;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.model.Message;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailAddress;
import io.soffa.foundation.models.mail.EmailId;
import io.soffa.foundation.pubsub.PubSubMessenger;
import io.soffa.foundation.messages.MessageFactory;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class NatsIntegrationTest {

    static {
        String natsUrl = System.getenv("NATS_URL");
        if (TextUtil.isNotEmpty(natsUrl)) {
            System.setProperty("app.pubsub.enabled", "true");
            System.setProperty("app.pubsub.clients.default.addresses", natsUrl);
            System.setProperty("app.pubsub.clients.default.subjects", "sample,foundation-service*");
            //System.setProperty("app.pubsub.clients.default.broadcasting", "foundation-service");
        }
    }

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private PubSubMessenger messenger;

    @Test
    public void testContext() {
        assertNotNull(applicationContext);
    }

    @SneakyThrows
    //@RepeatedTest(3)
    @Test
    @EnabledIfEnvironmentVariable(named = "NATS_URL", matches = ".+")
    public void testNatsIntegration() {
        assertNotNull(messenger);
        // Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> pubSubClient.isReady());

        AtomicLong counter = new AtomicLong(SendEmailHandler.COUNTER.get());
        Message event = MessageFactory.create(
            "SendEmail",
            new Email(
                "Hello world",
                EmailAddress.of("to@email.com"),
                "Text message",
                "<h1>Html message</h1>"
            )
        );

        EmailId response = messenger.request(applicationName, event, EmailId.class).get(20, TimeUnit.SECONDS);
        assertNotNull(response);
        assertEquals("000", response.getId());
        assertEquals(counter.incrementAndGet(), SendEmailHandler.COUNTER.get());

        messenger.broadcast("foundation-service", event);
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).until(() -> SendEmailHandler.COUNTER.get() == counter.incrementAndGet());

        messenger.broadcast("*", event);
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).until(() -> SendEmailHandler.COUNTER.get() == counter.incrementAndGet());

        Ping ping = messenger.proxy(applicationName, Ping.class);
        PingResponse resp = ping.handle(Operation.NO_INPUT, new RequestContext());
        assertEquals("PONG", resp.getValue());

        /*
        API binaryAPI = pubSubClient.createClient(API.class, applicationName);
        resp = binaryAPI.ping(new RequestContext());
        assertEquals("PONG", resp.getValue());
        */
    }


}
