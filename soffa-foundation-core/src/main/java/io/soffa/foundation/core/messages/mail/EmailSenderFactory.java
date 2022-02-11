package io.soffa.foundation.core.messages.mail;

import io.soffa.foundation.commons.UrlInfo;
import io.soffa.foundation.core.exceptions.NotImplementedException;
import io.soffa.foundation.core.messages.mail.adapters.FakeEmailSender;
import io.soffa.foundation.core.messages.mail.adapters.MailerConfig;
import io.soffa.foundation.core.messages.mail.adapters.SmtpEmailSender;

public final class EmailSenderFactory {

    private EmailSenderFactory() {}

    public static EmailSender create(String url, String defaultSender) {
        UrlInfo ui = UrlInfo.parse(url);
        if ("smtp".equalsIgnoreCase(ui.getProtocol())) {
            MailerConfig config = new MailerConfig();
            config.setSender(defaultSender);
            config.setHostname(ui.getHostname());
            config.setPort(ui.getPort());
            config.setTls(true);
            return new SmtpEmailSender(config);
        }else if ("faker".equalsIgnoreCase(ui.getProtocol())) {
            return new FakeEmailSender();
        }

        throw new NotImplementedException("Protocol not supported: " + ui.getProtocol());
    }

}
