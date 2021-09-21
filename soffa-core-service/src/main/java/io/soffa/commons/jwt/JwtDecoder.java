package io.soffa.commons.jwt;

import io.soffa.service.core.model.Authentication;

import java.util.Optional;

public interface JwtDecoder {

    Optional<Authentication> decode(String jwt);

}
