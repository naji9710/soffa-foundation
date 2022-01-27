package io.soffa.foundation.commons.jwt;

import io.soffa.foundation.core.model.Authentication;

public interface ClaimsExtractor {

    Authentication extractInfo(Jwt jwt);

}
