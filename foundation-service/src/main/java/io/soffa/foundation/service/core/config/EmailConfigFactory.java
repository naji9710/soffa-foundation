package io.soffa.foundation.service.core.config;

import io.soffa.foundation.core.email.EmailSender;
import io.soffa.foundation.core.email.adapters.SmtpEmailSender;
import io.soffa.foundation.core.email.model.MailerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "app.mail.provider", havingValue = "smtp")
public class EmailConfigFactory {

    @Bean
    @ConfigurationProperties(prefix = "app.mail")
    public MailerConfig createSmtpConfig() {
        return new MailerConfig();
    }

    @Bean
    @ConditionalOnBean(MailerConfig.class)
    @ConditionalOnMissingBean
    public EmailSender createEmailSender(MailerConfig config) {
        return new SmtpEmailSender(config);
    }

}
