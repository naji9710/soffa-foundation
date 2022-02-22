package ext.springboot;

import io.soffa.foundation.service.storage.ObjectStorageClient;
import io.soffa.foundation.service.storage.adapters.S3Client;
import io.soffa.foundation.service.storage.model.ObjectStorageConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "app.s3.enabled", havingValue = "true")
public class ObjectStorageAutoConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.s3")
    public ObjectStorageConfig createS3Config() {
        return new ObjectStorageConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectStorageClient createS3Client(ObjectStorageConfig config) {
        return new S3Client(config);
    }

}
