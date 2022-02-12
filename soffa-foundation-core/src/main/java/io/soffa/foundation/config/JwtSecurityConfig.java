package io.soffa.foundation.config;

import lombok.Data;

@Data
public class JwtSecurityConfig {

    private String issuer;
    private String secret;
    private String defaultTtl;
}
