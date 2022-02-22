package io.soffa.foundation.application.security;

import io.soffa.foundation.application.model.Authentication;
import io.soffa.foundation.application.model.Token;

public interface ClaimsExtractor {

    Authentication extractInfo(Token token);

}
