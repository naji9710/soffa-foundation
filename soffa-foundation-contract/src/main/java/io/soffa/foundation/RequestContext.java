package io.soffa.foundation;

import io.soffa.foundation.model.Authentication;
import io.soffa.foundation.model.TenantId;

public interface RequestContext {

    boolean isAuthenticated();

    Authentication getAuthentication();

    TenantId getTenantId();

    String getApplicationName();

    String getTraceId();

    String getSpanId();

    String getRequestId();

}
