package io.soffa.foundation.support.email;

import io.soffa.foundation.commons.UrlInfo;
import io.soffa.foundation.support.email.adapters.FakeEmailSender;
import io.soffa.foundation.support.email.adapters.MailerConfig;
import io.soffa.foundation.support.email.adapters.SmtpEmailSender;
import io.soffa.foundation.errors.NotImplementedException;

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
