package io.soffa.foundation.application.security;


import io.soffa.foundation.application.model.Authentication;
import io.soffa.foundation.application.model.Token;
import io.soffa.foundation.application.model.TokenType;
import io.soffa.foundation.application.security.model.TokensConfig;

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

    TokensConfig getConfig();

}
