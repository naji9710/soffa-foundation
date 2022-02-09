package io.soffa.foundation.spring.config;

import io.soffa.foundation.core.data.s3.ObjectStorageClient;
import io.soffa.foundation.core.data.s3.S3Client;
import io.soffa.foundation.core.data.s3.S3config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "app.s3.enabled", havingValue = "true")
public class S3ConfigFactory {

    @Bean
    @ConfigurationProperties(prefix = "app.s3")
    public S3config createS3Config() {
        return new S3config();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectStorageClient createS3Client(S3config config) {
        return new S3Client(config);
    }

}
