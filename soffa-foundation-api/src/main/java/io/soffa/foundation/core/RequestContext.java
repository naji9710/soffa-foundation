package io.soffa.foundation.core;

import io.soffa.foundation.core.models.Authentication;

import java.util.Map;
import java.util.Optional;

public interface RequestContext {

    String TENANT_ID = "X-TenantId";
    String APPLICATION = "X-Application";
    String TRACE_ID = "X-TraceId";
    String SPAN_ID = "X-SpanId";
    String SERVICE_NAME = "X-ServiceName";
    String AUTHORIZATION = "Authorization";

    Map<String, String> getHeaders();

    default void sync() {
        // TODO: Should we keep this ?
    }

    String getAuthorization();

    Map<String, String> getContextMap();

    String getTenantId();

    void setApplicationName(String value);

    void setTenantId(String value);

    void setTraceId(String value);

    void setSpanId(String value);

    void setSender(String value);

    void setAuthorization(String value);

    boolean isAuthenticated();

    String getApplicationName();

    String getSender();

    Optional<String> getUsername();

    boolean hasAuthorization();

    void setAuthentication(Authentication auth);

    Authentication getAuthentication();

    String getSpanId();

    String getTraceId();

    default RequestContext withAuthorization(String authorization) {
        setAuthorization(authorization);
        return this;
    }

    boolean hasTenant();

}
