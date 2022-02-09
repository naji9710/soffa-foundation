package io.soffa.foundation;

import io.soffa.foundation.core.messages.mail.EmailSender;
import io.soffa.foundation.core.messages.mail.adapters.MailerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
    "app.mail.provider=smtp",
    "app.mail.hostname=smtp.google.com:587",
    "app.mail.username=foo",
    "app.mail.password=s3cret",
})
@ActiveProfiles("test")
public class EmailClientTest {

    @Autowired
    private EmailSender client;

    @Autowired
    private MailerConfig config;

    @Test
    public void testMailerConfig() {
        assertNotNull(config);
        assertEquals("smtp.google.com", config.getHostname());
        assertEquals(587, config.getPort());
        assertEquals("foo", config.getUsername());
        assertEquals("s3cret", config.getPassword());
        assertNotNull(client);
    }

}
