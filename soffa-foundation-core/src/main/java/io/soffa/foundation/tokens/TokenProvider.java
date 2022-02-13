package io.soffa.foundation.tokens;


import io.soffa.foundation.model.Authentication;

import java.util.Map;

public interface TokenProvider {

    default Token create(TokenType type, String subject) {
        return create(type, subject, null);
    }

    Token create(TokenType type, String subject, Map<String, Object> claims);

    Token create(TokenType type, String subject, Map<String, Object> claims, int ttlInSeconds);

    Authentication decode(String token, ClaimsExtractor extractor);

    Authentication decode(String token);

    Authentication extractInfo(Token token);

    TokenConfig getConfig();

    String getServiceToken();

}
