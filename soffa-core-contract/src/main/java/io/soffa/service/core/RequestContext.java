package io.soffa.service.core;

import io.soffa.service.core.model.Authentication;
import io.soffa.service.core.model.TenantId;

public interface RequestContext {

    boolean isAuthenticated();

    Authentication getAuthentication();

    TenantId getTenantId();

    String getApplicationName();

    String getTraceId();

    String getSpanId();

    String getRequestId();

}
