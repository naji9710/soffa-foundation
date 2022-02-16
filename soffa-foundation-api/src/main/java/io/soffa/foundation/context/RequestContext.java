package io.soffa.foundation.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.soffa.foundation.api.ApiHeaders;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.model.Authentication;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Data
public class RequestContext {

    private static String serviceName = "app";
    private String authorization;
    private String tenantId;
    private String applicationName;
    private String sender;
    private String traceId;
    private String spanId;

    @JsonIgnore
    private transient Authentication authentication;

    public RequestContext() {
        this.traceId = UUID.randomUUID().toString();
        this.spanId = UUID.randomUUID().toString();
        this.sender = serviceName;
    }

    public static RequestContext create(String tenantId) {
        return new RequestContext().withTenant(tenantId);
    }

    public boolean hasAuthorization() {
        return authorization != null && !authorization.isEmpty();
    }

    @SneakyThrows
    public static void setServiceName(String value) {
        if (TextUtil.isEmpty(value)) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }
        serviceName = value;
    }

    public String getSender() {
        return sender;
    }

    public RequestContext withTenant(String tenantId) {
        this.setTenantId(tenantId);
        return this;
    }

    public RequestContext withAuthorization(String authorization) {
        this.authorization = authorization;
        return this;
    }

    public String getTenant() {
        if (tenantId == null) {
            return null;
        }
        return tenantId;
    }


    public boolean hasTenant() {
        return tenantId != null && !tenantId.isEmpty();
    }

    public boolean isAuthenticated() {
        return authentication != null;
    }

    public void setAuthentication(Authentication auth) {
        this.authentication = auth;
        if (auth == null) {
            return;
        }
        tenantId = auth.getTenantId();
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

    public void sync() {
        this.sender = serviceName;
    }


    @SneakyThrows
    public Map<String, String> getContextMap() {
        Map<String, String> contextMap = new HashMap<>();
        if (TextUtil.isNotEmpty(getApplicationName())) {
            contextMap.put("application", getApplicationName());
        }
        if (TextUtil.isNotEmpty(getTenant())) {
            contextMap.put("tenant", getTenant());
        }
        if (TextUtil.isNotEmpty(getTraceId())) {
            contextMap.put("traceId", getTraceId());
        }
        if (TextUtil.isNotEmpty(getSpanId())) {
            contextMap.put("spanId", getSpanId());
        }
        if (TextUtil.isNotEmpty(getSender())) {
            contextMap.put("sender", getSender());
        }
        if (getAuthentication() != null && TextUtil.isNotEmpty(getAuthentication().getUsername())) {
            contextMap.put("user", getAuthentication().getUsername());
        }
        contextMap.put("service_name", serviceName);
        return contextMap;
    }

    @SneakyThrows
    public static RequestContext fromHeaders(Map<String, String> headers) {
        RequestContext context = new RequestContext();
        if (headers == null) {
            return context;
        }
        for (Map.Entry<String, String> e : headers.entrySet()) {
            String value = e.getValue();
            if (TextUtil.isEmpty(value)) {
                continue;
            }
            String key = e.getKey();
            if (key.equalsIgnoreCase(ApiHeaders.APPLICATION)) {
                context.setApplicationName(value);
            } else if (key.equalsIgnoreCase(ApiHeaders.TENANT_ID)) {
                context.setTenantId(value);
            } else if (key.equalsIgnoreCase(ApiHeaders.SPAN_ID)) {
                context.setTraceId(value);
            } else if (key.equalsIgnoreCase(ApiHeaders.TRACE_ID)) {
                context.setSpanId(value);
            } else if (key.equalsIgnoreCase(ApiHeaders.SERVICE_NAME)) {
                context.setSender(value);
            } else if (key.equalsIgnoreCase(ApiHeaders.AUTHORIZATION)) {
                context.setAuthorization(value);
            }
        }
        return context;
    }

    @SneakyThrows
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        if (TextUtil.isNotEmpty(getApplicationName())) {
            headers.put(ApiHeaders.APPLICATION, getApplicationName());
        }
        if (TextUtil.isNotEmpty(getTenant())) {
            headers.put(ApiHeaders.TENANT_ID, getTenant());
        }
        if (TextUtil.isNotEmpty(getTraceId())) {
            headers.put(ApiHeaders.TRACE_ID, getTraceId());
        }
        if (TextUtil.isNotEmpty(getSpanId())) {
            headers.put(ApiHeaders.SPAN_ID, getSpanId());
        }
        if (TextUtil.isNotEmpty(getSender())) {
            headers.put(ApiHeaders.SERVICE_NAME, getSender());
        }
        if (TextUtil.isNotEmpty(getAuthorization())) {
            headers.put(ApiHeaders.AUTHORIZATION, getAuthorization());
        }
        return headers;
    }

}
