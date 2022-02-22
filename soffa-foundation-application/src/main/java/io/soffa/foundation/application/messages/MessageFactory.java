package io.soffa.foundation.application.messages;

import io.soffa.foundation.application.RequestContext;
import io.soffa.foundation.application.context.RequestContextHolder;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.ObjectUtil;
import lombok.SneakyThrows;

import java.util.Map;

public final class MessageFactory {

    private MessageFactory() {
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T getPayload(final Message message) {
        if (message.getPayload() == null) {
            return null;
        }
        return (T) JsonUtil.deserialize(message.getPayload(), Class.forName(message.getPayloadType()));
    }

    @SneakyThrows
    public static <T> T getPayload(final Message message, Class<T> type) {
        if (type == Void.class) {
            return null;
        }
        return JsonUtil.deserialize(message.getPayload(), type);
    }

    public static Message create(String operation, Object payload) {
        byte[] lPayload = null;
        String payloadType = null;
        if (payload != null) {
            lPayload = ObjectUtil.serialize(payload);
            payloadType = payload.getClass().getName();
        }
        RequestContext context = RequestContextHolder.inheritOrCreate();
        context.sync();
        Map<String, String> headers = context.getHeaders();
        return new Message(IdGenerator.shortUUID("msg"), operation, lPayload, payloadType, headers);
    }

    public static Message create(String operation) {
        return create(operation, null);
    }

}
