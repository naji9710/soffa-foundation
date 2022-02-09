package com.company.app;

import com.company.app.actions.SendEmailHandler;
import com.company.app.core.PingAction;
import com.company.app.core.PingResponse;
import com.company.app.gateways.API;
import io.soffa.foundation.client.RestClient;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.messages.BinaryClient;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailAddress;
import io.soffa.foundation.models.mail.EmailId;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class NatsIntegrationTest {

    static {
        String natsUrl = System.getenv("NATS_URL");
        if (TextUtil.isNotEmpty(System.getenv("NATS_URL"))) {
            System.setProperty("app.nats.enabled", "true");
            System.setProperty("app.nats.queue", "foundation-service");
            System.setProperty("app.nats.url", natsUrl);
        }
    }

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private BinaryClient binaryClient;

    @Test
    public void testContext() {
        assertNotNull(context);
    }

    @SneakyThrows
    @Test
    @EnabledIfEnvironmentVariable(named = "NATS_URL", matches = ".+")
    public void testNatsIntegration() {
        assertNotNull(binaryClient);
        long initialCounterValue = SendEmailHandler.COUNTER.get();
        Message event = new Message(
            "SendEmail",
            new Email("Hello world", EmailAddress.of("to@email.com"), "Text message", "<h1>Html message</h1>")
        );
        EmailId response = binaryClient.request(applicationName, event, EmailId.class).get(1, TimeUnit.SECONDS);
        assertNotNull(response);
        assertEquals("000", response.getId());
        assertEquals(initialCounterValue + 1, SendEmailHandler.COUNTER.get());

        binaryClient.broadcast(event);
        Awaitility.await().atMost(500, TimeUnit.DAYS).until(() -> SendEmailHandler.COUNTER.get() == initialCounterValue + 2);

        PingResponse resp = binaryClient.request(applicationName, PingAction.class).get(1, TimeUnit.SECONDS);
        assertEquals("PONG", resp.getValue());

        API binaryAPI = binaryClient.createClient(API.class, applicationName);
        resp = binaryAPI.ping(new RequestContext());
        assertEquals("PONG", resp.getValue());
    }


}
