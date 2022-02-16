package io.soffa.foundation.commons.http;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.TextUtil;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

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

    public String getMessageOrBody() {
        if (TextUtil.isNotEmpty(message)) {
            return message;
        }
        return body;
    }

    @SneakyThrows
    public boolean isOK() {
        return status == 200;
    }

    @SneakyThrows
    public boolean isBadRequest() {
        return status == 400;
    }

    @SneakyThrows
    public boolean is(int statusCode) {
        return statusCode == status;
    }

    @SneakyThrows
    public boolean is2xxSuccessful() {
        return status >= 200 && status < 300;
    }

    @SneakyThrows
    public boolean is3xxRedirection() {
        return status >= 300 && status < 400;
    }

    @SneakyThrows
    public boolean is4xxClientError() {
        return status >= 400 && status < 500;
    }

    @SneakyThrows
    public boolean is5xxServerError() {
        return status >= 500 && status < 600;
    }

    @SneakyThrows
    public boolean isForbidden() {
        return status == 403;
    }

    @SneakyThrows
    public boolean isUnauthorized() {
        return status == 401;
    }

    public <T> T json(String path) {
        try {
            return JsonPath.read(getBody(), path);
        } catch (PathNotFoundException e) {
            return null;
        }
    }


}
