package io.soffa.foundation.core;

import java.util.HashMap;
import java.util.Map;

public final class RequestContextUtil {

    private RequestContextUtil() {
    }

    public static Map<String, Object> tagify(RequestContext context) {
        return tagify(context, null);
    }

    public static Map<String, Object> tagify(RequestContext context, Map<String, Object> more) {
        Map<String, Object> tags = createTags(
            "ctx_authenticated", context.isAuthenticated(),
            "ctx_tenant", context.getTenant(),
            "ctx_span_id", context.getSpanId(),
            "ctx_trace_id", context.getTraceId(),
            "ctx_application", context.getApplicationName(),
            "ctx_username", context.getUsername()
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
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (!(args[i] instanceof String)) {
                throw new IllegalArgumentException("MapUtil.create() requires String keys");
            }
            result.put(args[i].toString(), args[i + 1]);
        }
        return result;
    }


}
