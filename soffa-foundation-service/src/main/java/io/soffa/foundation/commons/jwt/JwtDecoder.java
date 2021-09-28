package io.soffa.foundation.commons.jwt;

import io.soffa.foundation.model.Authentication;

import java.util.Optional;

public interface JwtDecoder {

    Optional<Authentication> decode(String jwt);

}
