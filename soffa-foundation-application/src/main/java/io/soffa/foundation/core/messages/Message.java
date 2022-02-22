package io.soffa.foundation.core.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.context.DefaultRequestContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;
import java.io.Serializable;
import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {

    public static final long serialVersionUID = -2355203729601016346L;
    private String id;
    private String operation;
    private byte[] payload;
    private String payloadType;
    private Map<String, String> headers;


    public Message(String operation,  RequestContext context) {
        this(null, operation, null, null, context);
    }


    public Message(String operation, Object payload, RequestContext context) {
        this(null, operation, payload, null, context);
    }

    public Message(String id, String operation, Object payload, String payloadType, RequestContext context) {
        this.id = TextUtil.isEmpty(id) ? IdGenerator.shortUUID("msg_") : id;
        this.operation = operation;
        this.payloadType = payloadType;
        if (context != null) {
            this.headers = context.getHeaders();
        }

        if (payload != null) {
            if (payload instanceof byte[]) {
                this.payload = (byte[]) payload;
            } else {
                this.payload = ObjectUtil.serialize(payload);
                this.payloadType = payload.getClass().getName();
            }
        }
    }

    @JsonIgnore
    @Transient
    public boolean hasHeaders() {
        return headers != null && !headers.isEmpty();
    }

    public  RequestContext getContext() {
        return DefaultRequestContext.fromHeaders(headers);
    }
}
