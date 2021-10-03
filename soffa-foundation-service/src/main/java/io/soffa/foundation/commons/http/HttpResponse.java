package io.soffa.foundation.commons.http;

import io.soffa.foundation.commons.JsonUtil;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HttpResponse {

    private int status;
    private String message;
    private String contentType;
    private String body;

    public static HttpResponse ok(String contentType, Object body) {
        return HttpResponse.builder().status(200).contentType(contentType).body(JsonUtil.serialize(body)).build();
    }

}
