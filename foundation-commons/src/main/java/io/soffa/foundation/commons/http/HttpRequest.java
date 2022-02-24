package io.soffa.foundation.commons.http;

import com.google.common.net.HttpHeaders;
import io.soffa.foundation.commons.TextUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class HttpRequest {

    private String method;
    private String url;
    private Object body;
    private Map<String, String> hdrs = new HashMap<>();
    private String contentType = "application/json";

    public HttpRequest(String method, String url) {
        this.url = url;
        this.method = method;
    }

    public HttpRequest(String method, String url, Object body) {
        this.method = method;
        this.url = url;
        this.body = body;
    }

    public static HttpRequest get(String url) {
        return new HttpRequest("GET", url);
    }

    public static HttpRequest delete(String url) {
        return new HttpRequest("DELETE", url);
    }

    public static HttpRequest patch(String url) {
        return new HttpRequest("PATCH", url);
    }

    public static HttpRequest put(String url) {
        return new HttpRequest("PUT", url);
    }

    public static HttpRequest post(String url) {
        return new HttpRequest("POST", url);
    }

    public static HttpRequest post(String url, Object body) {
        return new HttpRequest("POST", url, body);
    }

    public static HttpRequest delete(String url, Object body) {
        return new HttpRequest("DELETE", url, body);
    }

    public static HttpRequest patch(String url, Object body) {
        return new HttpRequest("PATCH", url, body);
    }

    public static HttpRequest put(String url, Object body) {
        return new HttpRequest("PUT", url, body);
    }

    public HttpRequest header(String name, String value) {
        this.hdrs.put(name, value);
        return this;
    }

    public HttpRequest headers(Map<String, String> headers) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (TextUtil.isNotEmpty(e.getValue())) {
                this.hdrs.put(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    public HttpRequest bearerAuth(String bearerToken) {
        this.hdrs.put(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        return this;
    }

}
