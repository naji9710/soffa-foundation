package io.soffa.foundation.context;

import io.soffa.foundation.api.ApiHeaders;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.model.Authentication;
import io.soffa.foundation.model.TenantId;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class RequestContext {

    private static String serviceName = "app";
    private final Map<String, Object> metas = new HashMap<>();
    private String authorization;
    private Authentication authentication;
    private TenantId tenantId;
    private String applicationName;
    private String traceId;
    private String spanId;

    public RequestContext() {
        this.traceId = UUID.randomUUID().toString();
        this.spanId = UUID.randomUUID().toString();
    }

    public static RequestContext create(String tenantId) {
        RequestContext ctx = new RequestContext();
        if (TextUtil.isNotEmpty(tenantId)) {
            ctx.setTenantId(new TenantId(tenantId));
        }
        return ctx;
    }

    public boolean hasAuthorization() {
        return TextUtil.isNotEmpty(authorization);
    }

    @SneakyThrows
    public static void setServiceName(String value) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }
        serviceName = value;
    }

    private static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public RequestContext withTenant(String tenantId) {
        this.setTenantId(new TenantId(tenantId));
        return this;
    }

    public String getTenant() {
        if (tenantId == null) {
            return null;
        }
        return tenantId.getValue();
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
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
        if (authentication != null && isNotEmpty(authentication.getUsername())) {
            contextMap.put("user", authentication.getUsername());
        }
        contextMap.put("service_name", serviceName);
        return contextMap;
    }

    @SneakyThrows
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (isNotEmpty(applicationName)) {
            headers.put(ApiHeaders.APPLICATION, applicationName);
        }
        if (tenantId != null) {
            headers.put(ApiHeaders.TENANT_ID, tenantId.getValue());
        }
        if (isNotEmpty(traceId)) {
            headers.put(ApiHeaders.TRACE_ID, traceId);
        }
        if (isNotEmpty(spanId)) {
            headers.put(ApiHeaders.SPAN_ID, spanId);
        }
        if (authorization != null && !authorization.isEmpty()) {
            headers.put("Authorization", "Bearer " + authorization);
        }
        return headers;
    }

}
