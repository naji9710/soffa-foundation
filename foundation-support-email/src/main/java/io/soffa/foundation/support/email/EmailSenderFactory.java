package io.soffa.foundation.support.email;

import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.commons.UrlInfo;
import io.soffa.foundation.errors.ConfigurationException;
import io.soffa.foundation.errors.NotImplementedException;
import io.soffa.foundation.support.email.adapters.FakeEmailSender;
import io.soffa.foundation.support.email.adapters.SendgridEmailSender;
import io.soffa.foundation.support.email.adapters.SmtpEmailSender;
import io.soffa.foundation.support.email.model.MailerConfig;

import java.util.Objects;

public final class EmailSenderFactory {

    private EmailSenderFactory() {
    }

    public static EmailSender create(String url, String defaultSender) {
        UrlInfo uri = UrlInfo.parse(url);
        String lDefaultSender = defaultSender;
        if (TextUtil.isEmpty(lDefaultSender)) {
            lDefaultSender = uri.param("from").orElse(null);
        }

        if ("smtp".equalsIgnoreCase(uri.getProtocol())) {
            MailerConfig config = new MailerConfig();
            config.setSender(lDefaultSender);
            config.setHostname(uri.getHostname());
            config.setPort(uri.getPort());
            boolean hasTlS = Objects.equals(uri.param("tls", "enabled"), "disabled");
            config.setTls(hasTlS);
            return new SmtpEmailSender(config);

        } else if ("faker".equalsIgnoreCase(uri.getProtocol())) {

            return new FakeEmailSender();

        } else if ("sendgrid".equalsIgnoreCase(uri.getProtocol())) {

            String apiKey = uri.getUsername();
            if (TextUtil.isEmpty(apiKey)) {
                apiKey = uri.param("apiKey").orElse(null);
            }
            if (TextUtil.isEmpty(apiKey)) {
                throw new ConfigurationException("Unable to locate Sendgrid apiKey");
            }
            return new SendgridEmailSender(apiKey, lDefaultSender);
        }

        throw new NotImplementedException("Protocol not supported: " + uri.getProtocol());
    }

}
