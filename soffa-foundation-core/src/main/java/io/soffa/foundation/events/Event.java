package io.soffa.foundation.events;

import com.google.common.base.Preconditions;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.model.TenantId;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Optional;

@Data
@AllArgsConstructor
public class Event implements Serializable {

    public static final long serialVersionUID = -2355203729601016346L;
    private static final Logger LOG = Logger.get(Event.class);
    private String id;
    private String action;
    private Object payload;
    private RequestContext context;

    public Event() {
        this.id = IdGenerator.secureRandomId("evt_");
        context = JsonUtil.clone(RequestContextHolder.get().orElse(new RequestContext()));
        if (TenantHolder.isNotEmpty()) {
            context.setTenantId(TenantId.of(TenantHolder.require()));
        }
    }

    public Event(String action) {
        this();
        this.action = action;
    }

    public Event(String action, Object payload) {
        this(action);
        this.payload = payload;
    }

    public Event(String action, Object payload, RequestContext context) {
        this.action = action;
        this.payload = payload;
        this.context = JsonUtil.clone(context);
    }

    public TenantId getTenantId() {
        if (context == null) {
            return null;
        }
        return context.getTenantId();
    }

    public <T> Optional<T> getPayloadAs(Class<T> expectedType) {
        Preconditions.checkNotNull(expectedType, "Invalid type provided");
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
        this.context = JsonUtil.clone(context);
        return this;
    }
}
