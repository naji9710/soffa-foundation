package io.soffa.foundation.context;

import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.exceptions.ConfigurationException;
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
    private static String serviceToken = "";

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

    @SneakyThrows
    public static void setServiceToken(String value) {
        serviceToken = value;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public RequestContext withTenant(String tenantId) {
        this.setTenantId(new TenantId(tenantId));
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
        this.authorization = AuthUtil.createBasicAuthorization(serviceName,serviceToken);
        return this;
    }

    public String getTenant() {
        if (tenantId == null) {
            return null;
        }
        return tenantId.getValue();
    }

    public String getServiceName() {
        return serviceName;
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



}
