package io.soffa.foundation.security;

import io.soffa.foundation.model.Authentication;
import io.soffa.foundation.security.model.Token;

public interface ClaimsExtractor {

    Authentication extractInfo(Token token);

}
