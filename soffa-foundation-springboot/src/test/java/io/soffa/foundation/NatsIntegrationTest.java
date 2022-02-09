package io.soffa.foundation;

import io.soffa.foundation.events.Event;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailAddress;
import io.soffa.foundation.models.mail.EmailId;
import io.soffa.foundation.pubsub.BinaryClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(properties = {
    "spring.application.name=test",
    "app.nats.enabled=true",
    "app.nats.url=${NATS_URL}"
})
@ActiveProfiles("test")
public class NatsIntegrationTest {

    @Autowired(required = false)
    private BinaryClient binaryClient;

    @Test
    @SneakyThrows
    @EnabledIfEnvironmentVariable(named = "NATS_URL", matches = ".+")
    public void testNatsIntegration() {
        assertNotNull(binaryClient);
        Event event = new Event(
            "SendEmail",
            new Email("Hello world", EmailAddress.of("to@email.com"), "Text message", "<h1>Html message</h1>")
        );
        EmailId response = binaryClient.request("test", event, EmailId.class).get();
        assertNotNull(response);
        assertEquals("000", response.getId());
    }

}
