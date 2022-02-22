package io.soffa.foundation.core.security;

import io.soffa.foundation.core.models.Authentication;
import io.soffa.foundation.models.Token;

public interface ClaimsExtractor {

    Authentication extractInfo(Token token);

}
