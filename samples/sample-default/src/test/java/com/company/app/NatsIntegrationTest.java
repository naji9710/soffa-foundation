package com.company.app;

import com.company.app.core.Ping;
import com.company.app.core.PingResponse;
import com.company.app.gateways.API;
import com.company.app.operations.SendEmailHandler;
import io.soffa.foundation.api.Operation;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.messages.Message;
import io.soffa.foundation.messages.PubSubClient;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailAddress;
import io.soffa.foundation.models.mail.EmailId;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.RepeatedTest;
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
public class NatsIntegrationTest   {

    static {
        String natsUrl = System.getenv("NATS_URL");
        if (TextUtil.isNotEmpty(natsUrl)) {
            System.setProperty("app.nats.enabled", "true");
            System.setProperty("app.nats.clients.default.addresses", natsUrl);
            System.setProperty("app.nats.clients.default.broadcast", "foundation-service");
        }
    }

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private PubSubClient pubSubClient;

    @Test
    public void testContext() {
        assertNotNull(context);
    }

    @SneakyThrows
    @RepeatedTest(3)
    @EnabledIfEnvironmentVariable(named = "NATS_URL", matches = ".+")
    public void testNatsIntegration() {
        assertNotNull(pubSubClient);
        Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> pubSubClient.isReady());

        AtomicLong counter = new AtomicLong(SendEmailHandler.COUNTER.get());
        Message event = new Message(
            "SendEmail",
            new Email("Hello world", EmailAddress.of("to@email.com"), "Text message", "<h1>Html message</h1>")
        );

        EmailId response = pubSubClient.request(applicationName, event, EmailId.class).get(20, TimeUnit.SECONDS);
        assertNotNull(response);
        assertEquals("000", response.getId());
        assertEquals(counter.incrementAndGet(), SendEmailHandler.COUNTER.get());

        pubSubClient.broadcast(event);
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).until(() -> SendEmailHandler.COUNTER.get() == counter.incrementAndGet());

        PingResponse resp = pubSubClient.request(applicationName, Ping.class, Operation.NO_INPUT).get(500, TimeUnit.MILLISECONDS);
        assertEquals("PONG", resp.getValue());

        API binaryAPI = pubSubClient.createClient(API.class, applicationName);
        resp = binaryAPI.ping(new RequestContext());
        assertEquals("PONG", resp.getValue());
    }


}
