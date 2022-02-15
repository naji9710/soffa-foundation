package io.soffa.foundation.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.exceptions.ConfigurationException;
import io.soffa.foundation.model.Authentication;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.UUID;

@Getter
public class RequestContext {

    private static String serviceName = "app";
    private String authorization;
    private String tenantId;
    private String applicationName;
    private String sender;
    private String traceId;
    private String spanId;

    @JsonIgnore
    private static transient String serviceToken = "";
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
        return TextUtil.isNotEmpty(authorization);
    }

    @SneakyThrows
    public static void setServiceName(String value) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }
        serviceName = value;
    }

    public String getSender() {
        return sender;
    }

    @SneakyThrows
    public static void setServiceToken(String value) {
        serviceToken = value;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public RequestContext withTenant(String tenantId) {
        this.setTenantId(tenantId);
        return this;
    }

    public RequestContext withAuthorization(String authorization) {
        this.authorization = authorization;
        return this;
    }

    public RequestContext withAuthorization(String username, String password) {
        this.authorization = AuthUtil.createBasicAuthorization(username, password);
        return this;
    }

    public RequestContext withServiceToken() {
        if (TextUtil.isEmpty(serviceToken)) {
            throw new ConfigurationException("Service token is not configured");
        }
        if (TextUtil.isEmpty(serviceName)) {
            throw new ConfigurationException("Service name is not configured");
        }
        this.authorization = AuthUtil.createBasicAuthorization(serviceName, serviceToken);
        return this;
    }

    public String getTenant() {
        if (tenantId == null) {
            return null;
        }
        return tenantId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public boolean hasTenant() {
        return TextUtil.isNotEmpty(tenantId);
    }

    public boolean isAuthenticated() {
        return authentication != null;
    }

    public void setTenantId(String tenantId) {
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
}
