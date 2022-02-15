package io.soffa.foundation.context;

import io.soffa.foundation.api.ApiHeaders;
import io.soffa.foundation.commons.TextUtil;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RequestContextUtil {

    private RequestContextUtil() {
    }

    public static Map<String, Object> tagify(RequestContext context) {
        return tagify(context, null);
    }

    public static Map<String, Object> tagify(RequestContext context, Map<String, Object> more) {
        String sessionId = context.getAuthorization();
        if (TextUtil.isNotEmpty(sessionId)) {
            sessionId = DigestUtils.md5Hex(sessionId);
        }
        Map<String, Object> tags = createTags(
            "ctx_access", context.isAuthenticated() ? "authenticated" : "anonymous",
            "ctx_tenant", context.getTenant(),
            "ctx_application", context.getApplicationName(),
            "ctx_source", context.getSender(),
            "ctx_username", context.getUsername(),
            "ctx_session_id", sessionId
        );
        if (more != null) {
            tags.putAll(more);
        }
        return tags;
    }

    @SuppressWarnings("DuplicatedCode")
    private static Map<String, Object> createTags(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("MapUtil.create() requires an even number of arguments");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (!(args[i] instanceof String)) {
                throw new IllegalArgumentException("MapUtil.create() requires String keys");
            }
            result.put(args[i].toString(), args[i + 1]);
        }
        return result;
    }


    @SneakyThrows
    public static Map<String, String> getContextMap(RequestContext context) {
        Map<String, String> contextMap = new HashMap<>();
        if (TextUtil.isNotEmpty(context.getApplicationName())) {
            contextMap.put("application", context.getApplicationName());
        }
        if (TextUtil.isNotEmpty(context.getTenant())) {
            contextMap.put("tenant", context.getTenant());
        }
        if (TextUtil.isNotEmpty(context.getTraceId())) {
            contextMap.put("traceId", context.getTraceId());
        }
        if (TextUtil.isNotEmpty(context.getSpanId())) {
            contextMap.put("spanId", context.getSpanId());
        }
        if (TextUtil.isNotEmpty(context.getSender())) {
            contextMap.put("sender", context.getSender());
        }
        if (context.getAuthentication() != null && TextUtil.isNotEmpty(context.getAuthentication().getUsername())) {
            contextMap.put("user", context.getAuthentication().getUsername());
        }
        contextMap.put("service_name", context.getServiceName());
        return contextMap;
    }

    @SneakyThrows
    public static Map<String, String> getHeaders(RequestContext context) {
        Map<String, String> headers = new HashMap<>();

        if (TextUtil.isNotEmpty(context.getApplicationName())) {
            headers.put(ApiHeaders.APPLICATION, context.getApplicationName());
        }
        if (TextUtil.isNotEmpty(context.getTenant())) {
            headers.put(ApiHeaders.TENANT_ID, context.getTenant());
        }
        if (TextUtil.isNotEmpty(context.getTraceId())) {
            headers.put(ApiHeaders.TRACE_ID, context.getTraceId());
        }
        if (TextUtil.isNotEmpty(context.getSpanId())) {
            headers.put(ApiHeaders.SPAN_ID, context.getSpanId());
        }
        if (TextUtil.isNotEmpty(context.getSender())) {
            headers.put(ApiHeaders.SERVICE_NAME, context.getSender());
        }
        if (TextUtil.isNotEmpty(context.getAuthorization())) {
            headers.put("Authorization", context.getAuthorization());
        }
        return headers;
    }
}
