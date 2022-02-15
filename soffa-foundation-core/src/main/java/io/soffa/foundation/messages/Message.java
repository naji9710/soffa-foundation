package io.soffa.foundation.messages;

import com.google.common.base.Preconditions;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.model.NoInput;
import io.soffa.foundation.model.TenantId;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Optional;

@Data
@AllArgsConstructor
public class Message implements Serializable {

    public static final long serialVersionUID = -2355203729601016346L;
    private static final Logger LOG = Logger.get(Message.class);
    private String id;
    private String operation;
    private Object payload;
    private RequestContext context;

    public Message() {
        this.id = IdGenerator.secureRandomId("msg_");
        context = JsonUtil.clone(RequestContextHolder.get().orElse(new RequestContext()));
        if (TenantHolder.isNotEmpty()) {
            context.setTenantId(TenantId.of(TenantHolder.require()));
        }
    }

    public Message(String operation) {
        this();
        this.operation = operation;
    }

    public Message(String operation, Object payload) {
        this(operation);
        this.payload = payload;
    }

    public Message(String operation, Object payload, RequestContext context) {
        this.operation = operation;
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
        if (expectedType == Void.class || expectedType == NoInput.class) {
            return Optional.empty();
        }
        if (payload.getClass() == expectedType) {
            return Optional.of(expectedType.cast(payload));
        }
        try {
            return Optional.ofNullable(JsonUtil.convert(payload, expectedType));
        } catch (Exception e) {
            LOG.error("Error unwrapping payload", e);
            return Optional.empty();
        }
    }

    public Message withApplication(String application) {
        context.setApplicationName(application);
        return this;
    }

    public Message withTenant(String tenant) {
        return withTenant(new TenantId(tenant));
    }

    public Message withTenant(TenantId tenant) {
        context.setTenantId(tenant);
        return this;
    }

    public Message withContext(String application, TenantId tenantId) {
        return withApplication(application).withTenant(tenantId);
    }

    public Message withContext(RequestContext context) {
        this.context = JsonUtil.clone(context);
        return this;
    }
}
