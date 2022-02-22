package io.soffa.foundation.application.security;

import io.soffa.foundation.application.RequestContext;
import io.soffa.foundation.application.model.Authentication;

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
