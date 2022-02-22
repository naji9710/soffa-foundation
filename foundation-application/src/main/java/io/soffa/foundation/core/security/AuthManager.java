package io.soffa.foundation.core.security;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.models.Authentication;

public interface AuthManager {

    default Authentication authenticate(RequestContext context) {
        return authenticate(context, context.getAuthorization());
    }

    default Authentication authenticate(RequestContext context, String token) {
        return null;
    }

    default Authentication authenticate(RequestContext context, String username, String password) {
        return null;
    }

}
