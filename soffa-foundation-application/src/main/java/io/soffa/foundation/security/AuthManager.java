package io.soffa.foundation.security;

import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.model.Authentication;

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
