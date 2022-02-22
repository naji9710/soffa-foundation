package io.soffa.foundation.application.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.soffa.foundation.application.RequestContext;
import io.soffa.foundation.application.model.Authentication;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.exposition.ApiHeaders;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Data
public class DefaultRequestContext implements RequestContext {

    private static String serviceName = "app";
    private String authorization;
    private String tenantId;
    private String applicationName;
    private String sender;
    private String traceId;
    private String spanId;

    @JsonIgnore
    private transient Authentication authentication;

    public DefaultRequestContext() {
        this.traceId = UUID.randomUUID().toString();
        this.spanId = UUID.randomUUID().toString();
        this.sender = serviceName;
    }

    public static RequestContext create(String tenantId) {
        return new DefaultRequestContext().withTenant(tenantId);
    }

    @Override
    public boolean hasAuthorization() {
        return TextUtil.isNotEmpty(authorization);
    }

    @SneakyThrows
    public static void setServiceName(String value) {
        if (TextUtil.isEmpty(value)) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }
        serviceName = value;
    }

    @Override
    public String getSender() {
        return sender;
    }

    public RequestContext withTenant(String tenantId) {
        this.setTenantId(tenantId);
        return this;
    }

    @Override
    public String getTenantId() {
        return TextUtil.trimToNull(tenantId);
    }

    @Override
    public boolean hasTenant() {
        return TextUtil.isNotEmpty(tenantId);
    }

    @Override
    public boolean isAuthenticated() {
        return authentication != null;
    }

    @Override
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

    @Override
    public Optional<String> getUsername() {
        if (authentication != null) {
            return Optional.ofNullable(authentication.getUsername());
        }
        return Optional.empty();
    }

    @Override
    public void sync() {
        this.sender = serviceName;
    }


    @Override
    @SneakyThrows
    public Map<String, String> getContextMap() {
        Map<String, String> contextMap = new HashMap<>();
        if (TextUtil.isNotEmpty(getApplicationName())) {
            contextMap.put("application", getApplicationName());
        }
        if (hasTenant()) {
            contextMap.put("tenant", getTenantId());
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
        RequestContext context = new DefaultRequestContext();
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

    @Override
    @SneakyThrows
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        if (TextUtil.isNotEmpty(getApplicationName())) {
            headers.put(ApiHeaders.APPLICATION, getApplicationName());
        }
        if (hasTenant()) {
            headers.put(ApiHeaders.TENANT_ID, getTenantId());
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
