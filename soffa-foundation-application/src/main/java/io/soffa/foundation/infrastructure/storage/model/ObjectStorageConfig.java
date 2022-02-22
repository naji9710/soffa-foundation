package io.soffa.foundation.infrastructure.storage.model;

import lombok.Data;

@Data
public class ObjectStorageConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;

}
