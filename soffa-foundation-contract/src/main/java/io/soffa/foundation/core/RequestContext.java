package io.soffa.foundation.core;

import io.soffa.foundation.core.model.Authentication;
import io.soffa.foundation.core.model.TenantId;
import io.soffa.foundation.exceptions.TechnicalException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@NoArgsConstructor
public class RequestContext {

    private static String serviceName = "app";

    private Authentication authentication;
    private TenantId tenantId;
    private String applicationName;
    private String traceId;
    private String spanId;
    private String requestId;
    private final Map<String, Object> metas = new HashMap<>();

    @SneakyThrows
    public static void setServiceName(String value) {
        if (isEmpty(value)) {
            throw new TechnicalException("Service name cannot be empty");
        }
        serviceName = value;
    }

    public boolean hasTenant() {
        return tenantId != null;
    }

    public boolean isAuthenticated() {
        return authentication != null;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setMeta(String key, Object value) {
        metas.put(key, value);
    }

    public void setAuthentication(Authentication auth) {
        this.authentication = auth;
        if (auth == null) {
            return;
        }
        if (auth.getTenantId() != null && auth.getTenantId().getValue() != null) {
            tenantId = auth.getTenantId();
        }
        if (auth.getApplication() != null && !auth.getApplication().isEmpty()) {
            applicationName = auth.getApplication();
        }
    }

    public Optional<String> getUsername() {
        if (authentication != null) {
            return Optional.ofNullable(authentication.getUsername());
        }
        return Optional.empty();
    }

    @SneakyThrows
    public Map<String, String> getContextMap() {
        Map<String, String> contextMap = new HashMap<>();
        if (isNotEmpty(applicationName)) {
            contextMap.put("application", applicationName);
        }
        if (tenantId != null) {
            contextMap.put("tenant", tenantId.getValue());
        }
        if (isNotEmpty(traceId)) {
            contextMap.put("traceId", traceId);
        }
        if (isNotEmpty(spanId)) {
            contextMap.put("spanId", spanId);
        }
        if (isNotEmpty(requestId)) {
            contextMap.put("requestId", requestId);
        }
        if (authentication != null && isNotEmpty(authentication.getUsername())) {
            contextMap.put("user", authentication.getUsername());
        }
        contextMap.put("service_name", serviceName);
        return contextMap;
    }

    private static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    private static  boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
