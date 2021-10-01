package io.soffa.foundation.jwt;

import io.soffa.foundation.core.model.Authentication;

import java.util.Optional;

public interface JwtDecoder {

    Optional<Authentication> decode(String jwt);

}
