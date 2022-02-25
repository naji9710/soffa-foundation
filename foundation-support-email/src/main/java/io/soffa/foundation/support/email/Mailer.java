package io.soffa.foundation.support.email;

import io.soffa.foundation.support.email.model.Email;
import io.soffa.foundation.support.email.model.EmailAck;

import java.util.Map;

public class Mailer implements EmailSender {

    private final Map<String, EmailSender> clients;
    private EmailSender defaultSender;
    public static final String DEFAULT = "default";

    public Mailer(Map<String, EmailSender> clients) {
        this.clients = clients;
        if (clients != null && !clients.isEmpty()) {
            defaultSender = clients.get(DEFAULT);
            if (defaultSender == null) {
                defaultSender = clients.values().iterator().next();
            }
        }
    }

    public EmailSender getClient(String id) {
        return clients.get(id);
    }

    @Override
    public EmailAck send(Email message) {
        return defaultSender.send(message);
    }
}
