package io.soffa.foundation.tokens;


import io.soffa.foundation.commons.TextUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenConfig {

    private String issuer;
    private String secret;
    private String serviceToken;
    private String publicJwks;
    private String privateJwks;
    private int defaultTtl = 60;

    public TokenConfig(String issuer, String secret) {
        this.issuer = issuer;
        this.secret = secret;
    }

    public boolean isActive() {
        return TextUtil.isNotEmpty(secret) || TextUtil.isNotEmpty(publicJwks);
    }

}
