package io.soffa.foundation.core.security;

import io.soffa.foundation.core.RequestContext;

public interface PlatformAuthManager {

    void handle(RequestContext context);

    void handle(RequestContext context, String value);

}
