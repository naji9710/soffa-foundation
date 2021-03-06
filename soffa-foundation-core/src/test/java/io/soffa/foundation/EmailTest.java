package io.soffa.foundation;

import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.commons.UrlInfo;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailAddress;
import io.soffa.foundation.support.mail.EmailSender;
import io.soffa.foundation.support.mail.EmailSenderFactory;
import io.soffa.foundation.support.mail.adapters.FakeEmailSender;
import io.soffa.foundation.support.mail.adapters.SmtpEmailSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailTest {

    @Test
    public void testEmailAddress() {

        String address = "noreply@email.local";

        EmailAddress email = new EmailAddress(" noreply@email.local ");
        assertNull(email.getName());
        assertEquals(address, email.getAddress());

        email = new EmailAddress(" <noreply@email.local> ");
        assertTrue(TextUtil.isEmpty(email.getName()));
        assertEquals(address, email.getAddress());

        email = new EmailAddress("John Doe <noreply@email.local> ");
        assertEquals("John Doe", email.getName());
        assertEquals(address, email.getAddress());

        email = new EmailAddress("\"John Doe\" <noreply@email.local> ");
        assertEquals("John Doe", email.getName());
        assertEquals(address, email.getAddress());

    }

    @Test
    public void testFakeEmailSender() {
        int counter = FakeEmailSender.getCounter();
        EmailSender sender = new FakeEmailSender();
        sender.send(
            new Email("Hello mailer", EmailAddress.of("noreply@local.dev"), "Hello world!", null)
        );
        assertEquals(counter + 1, FakeEmailSender.getCounter());
    }


    @Test
    public void testUrlParsing() {
        UrlInfo url = UrlInfo.parse("smtp://user:pass@mail.google.com:963?tls=true");
        assertNotNull(url);
        assertEquals("smtp", url.getProtocol());
        assertNotNull(url.getParams());
        assertEquals("user", url.getUsername());
        assertEquals("pass", url.getPassword());
        assertEquals("mail.google.com", url.getHostname());
        assertEquals(963, url.getPort());
        assertEquals("true", url.param("tls"));
    }

    @Test
    public void testEmailSenderFactory() {
        EmailSender sender = EmailSenderFactory.create("smtp://user:pass@mail.google.com:963?tls=true", "noreply@local.dev");
        assertNotNull(sender);
        assertEquals(SmtpEmailSender.class, sender.getClass());

        sender = EmailSenderFactory.create("faker://local", "noreply@local.dev");
        assertNotNull(sender);
        assertEquals(FakeEmailSender.class, sender.getClass());
    }

}
