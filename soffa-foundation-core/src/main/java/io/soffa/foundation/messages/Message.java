package io.soffa.foundation.messages;

import com.google.common.base.Preconditions;
import io.soffa.foundation.commons.*;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.model.TenantId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.Map;


@Data
@AllArgsConstructor
public class Message implements Serializable {

    public static final long serialVersionUID = -2355203729601016346L;
    private static final Logger LOG = Logger.get(Message.class);
    private String id;
    private String operation;
    private byte[] payload;
    private String payloadType;
    private RequestContext context;

    public Message() {
        this.id = IdGenerator.secureRandomId("msg_");
        context = JsonUtil.clone(RequestContextHolder.get().orElse(new RequestContext()));
        if (TenantHolder.isNotEmpty()) {
            context.setTenantId(TenantHolder.require());
        }
    }

    public Message(String operation) {
        this();
        this.operation = operation;
    }

    public Message(String operation, Object payload) {
        this(operation);
        if (payload != null) {
            payloadType = payload.getClass().getName();
            this.payload = ObjectUtil.serialize(payload);
        }

    }

    public Message(String operation, Object payload, RequestContext context) {
        this(operation, payload);
        this.context = JsonUtil.clone(context);
    }

    public TenantId getTenantId() {
        if (context == null) {
            return null;
        }
        return new TenantId(context.getTenantId());
    }

    @SneakyThrows
    public Object deserialize() {
        if (payload == null) {
            return null;
        }
        return this.getPayloadAs(Class.forName(payloadType));
    }

    public <T> T getPayloadAs(Class<T> expectedType) {
        Preconditions.checkNotNull(expectedType, "Invalid type provided");
        if (payload == null) {
            return null;
        }
        return ObjectUtil.deserialize(payload, expectedType);
    }

    public Message withApplication(String application) {
        context.setApplicationName(application);
        return this;
    }

    public Message withTenant(String tenant) {
        context.setTenantId(tenant);
        return this;
    }

    public Message withTenant(TenantId tenant) {
        context.setTenantId(tenant != null ? tenant.getValue() : null);
        return this;
    }

    public Message withContext(String application, TenantId tenantId) {
        return withApplication(application).withTenant(tenantId);
    }

    public Message withContext(RequestContext context) {
        this.context = JsonUtil.clone(context);
        return this;
    }

    public Map<String, Object> getTags(String subject) {
        return MapUtil.create("subject", subject, "operation", operation);
    }
}
