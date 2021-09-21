package io.soffa.service.model;

import io.soffa.service.core.RequestContext;
import io.soffa.service.core.model.Authentication;
import io.soffa.service.core.model.TenantId;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DefaultRequestContext implements RequestContext {


    private Authentication authentication;
    private TenantId tenantId;
    private String applicationName;
    private String traceId;
    private String spanId;
    private String requestId;
    private final Map<String, Object> metas = new HashMap<>();

    @Override
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
        if (auth.getTenantId() != null) {
            tenantId = auth.getTenantId();
        }
        if (StringUtils.isNotBlank(auth.getApplication())) {
            applicationName = auth.getApplication();
        }
    }
}
