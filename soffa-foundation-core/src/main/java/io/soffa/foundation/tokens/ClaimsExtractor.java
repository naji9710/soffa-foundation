package io.soffa.foundation.tokens;

import io.soffa.foundation.model.Authentication;

public interface ClaimsExtractor {

    Authentication extractInfo(Token token);

}
