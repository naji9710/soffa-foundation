package io.soffa.foundation.application.security;

import io.soffa.foundation.application.RequestContext;

public interface PlatformAuthManager {

    void handle(RequestContext context);

    void handle(RequestContext context, String value);

}
