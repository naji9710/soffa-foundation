package io.soffa.foundation.core.security;


import io.soffa.foundation.core.models.Authentication;
import io.soffa.foundation.core.security.model.TokensConfig;
import io.soffa.foundation.models.Token;
import io.soffa.foundation.models.TokenType;

import java.util.Map;

public interface TokenProvider {

    default Token create(TokenType type, String subject) {
        return create(type, subject, null);
    }

    Token create(TokenType type, String subject, Map<String, Object> claims);

    Token create(TokenType type, String subject, Map<String, Object> claims, int ttlInMinutes);

    Authentication decode(String token, ClaimsExtractor extractor);

    Authentication decode(String token);

    Authentication extractInfo(Token token);

    TokensConfig getConfig();

}
