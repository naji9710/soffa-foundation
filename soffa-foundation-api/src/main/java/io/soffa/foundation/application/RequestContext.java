package io.soffa.foundation.application;

import io.soffa.foundation.application.model.Authentication;

import java.util.Map;
import java.util.Optional;

public interface RequestContext {

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
