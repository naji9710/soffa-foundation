package io.soffa.foundation.core.security;

import io.soffa.foundation.commons.jwt.JwtDecoder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.model.Authentication;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class DefaultAuthorizationManager implements AuthManager {

    private final JwtDecoder jwtDecoder;

    @Override
    public Optional<Authentication> authenticate(RequestContext context, String token) {
        if (jwtDecoder == null) {
            return Optional.empty();
        }
        return jwtDecoder.decode(token);
    }

    @Override
    public Optional<Authentication> authenticate(RequestContext context, String username, String password) {
        return Optional.empty();
    }

}
