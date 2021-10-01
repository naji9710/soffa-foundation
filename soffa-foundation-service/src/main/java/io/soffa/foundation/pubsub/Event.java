package io.soffa.foundation.pubsub;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.model.TenantId;
import io.soffa.foundation.logging.Logger;
import io.soffa.foundation.support.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event implements Serializable {

    public static final long serialVersionUID = -2355203729601016346L;
    private static final Logger LOG = Logger.create(Event.class);
    private String action;
    private Object payload;
    private RequestContext context = new RequestContext();

    public Event(String action) {
        this.action = action;
    }

    public Event(String action, Object payload) {
        this.action = action;
        this.payload = payload;
    }

    public TenantId getTenantId() {
        if (context == null) {
            return null;
        }
        return context.getTenantId();
    }

    public <T> Optional<T> getPayloadAs(Class<T> expectedType) {
        if (payload == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(JsonUtil.convert(payload, expectedType));
        } catch (Exception e) {
            LOG.error("Error unwrapping payload", e);
            return Optional.empty();
        }
    }

    public Event withApplication(String application) {
        context.setApplicationName(application);
        return this;
    }

    public Event withTenant(String tenant) {
        return withTenant(new TenantId(tenant));
    }

    public Event withTenant(TenantId tenant) {
        context.setTenantId(tenant);
        return this;
    }

    public Event withContext(String application, TenantId tenantId) {
        return withApplication(application).withTenant(tenantId);
    }

    public Event withContext(RequestContext context) {
        this.context = context;
        return this;
    }
}
