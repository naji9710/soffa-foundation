package io.soffa.foundation.messages.mail.adapters;

import io.soffa.foundation.commons.CollectionUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.messages.mail.EmailSender;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailAddress;
import io.soffa.foundation.models.mail.EmailId;
import lombok.SneakyThrows;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.jetbrains.annotations.NotNull;

public class SmtpEmailSender implements EmailSender {

    private static final Logger LOG = Logger.get(SmtpEmailSender.class);
    private final MailerConfig config;

    public SmtpEmailSender(MailerConfig config) {
        config.afterPropertiesSet();
        this.config = config;
    }

    @SneakyThrows
    @Override
    public EmailId send(Email message) {
        check(message);
        HtmlEmail email = createMessage(message);

        return new EmailId(email.send());
    }

    @NotNull
    private HtmlEmail createMessage(Email message) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        email.setHostName(config.getHostname()); // ERROR
        email.setSmtpPort(config.getPort());
        email.setStartTLSEnabled(config.isTls());
        if (config.hasCredentials()) {
            email.setAuthenticator(new DefaultAuthenticator(
                config.getUsername(),
                config.getPassword()
            ));
        }
        email.setDebug(LOG.isDebugEnabled());

        if (message.getSender() != null) {
            email.setFrom(message.getSender().getAddress(), message.getSender().getName());
        } else {
            email.setFrom(config.getSender());
        }
        email.setHtmlMsg(message.getHtmlMessage());
        email.setTextMsg(message.getTextMessage());
        email.setSubject(message.getSubject());

        for (EmailAddress to : message.getTo()) {
            email.addTo(to.getAddress(), to.getName());
        }
        if (message.getCc() != null) {
            for (EmailAddress cc : message.getCc()) {
                email.addCc(cc.getAddress(), cc.getName());
            }
        }
        if (message.getBcc() != null) {
            for (EmailAddress bcc : message.getBcc()) {
                email.addBcc(bcc.getAddress(), bcc.getName());
            }
        }
        return email;
    }

    void check(Email message) {
        boolean hasSender = TextUtil.isNotEmpty(config.getSender()) || message.getSender() != null;

        if (!hasSender) {
            throw new TechnicalException("Missing sender (specify one in the message or in the global smtp config)");
        }
        if (TextUtil.isEmpty(message.getSubject())) {
            throw new TechnicalException("Email subject is required");
        }
        if (!message.hasMessage()) {
            throw new TechnicalException("Email content is required");
        }
        if (CollectionUtil.isEmpty(message.getTo())) {
            throw new TechnicalException("No recipients provided");
        }
    }

}
