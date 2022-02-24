package io.soffa.foundation.commons.http;

import io.soffa.foundation.commons.JsonUtil;
import lombok.SneakyThrows;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpClient {


    private static HttpClient defaultInstance = new HttpClient();
    private final OkHttpClient client;

    public HttpClient() {
        client = HttpUtil.newOkHttpClient();
    }

    public HttpClient(OkHttpClient client) {
        this.client = client;
    }

    public void setDefaultInstance(HttpClient instance) {
        defaultInstance = instance;
    }

    public static HttpClient getInstance() {
        return defaultInstance;
    }

    @SneakyThrows
    public HttpResponse request(HttpRequest req) {
        RequestBody body = null;


        if (req.getBody() != null) {
            body = RequestBody.create(JsonUtil.serialize(req.getBody()), MediaType.parse(req.getContentType()));
        }

        Map<String, String> hds = Optional.ofNullable(req.getHdrs()).orElse(new HashMap<>());
        hds.put("Content-Type", req.getContentType());
        Headers headers = Headers.of(hds);
        Request request = new Request.Builder()
            .url(req.getUrl())
            .method(req.getMethod(), body)
            .headers(headers)
            .build();
        Call call = client.newCall(request);
        try (Response result = call.execute()) {
            HttpResponse.HttpResponseBuilder res = HttpResponse.builder()
                .status(result.code())
                .message(result.message());
            try (ResponseBody responseBody = result.body()) {
                if (responseBody != null) {
                    MediaType contenType = responseBody.contentType();
                    if (contenType != null) {
                        res.contentType(contenType.type() + "/" + contenType.subtype());
                    }
                    res.body(responseBody.string());
                }
            }
            return res.build();
        }
    }

    public HttpResponse get(String url) {
        return this.request(HttpRequest.get(url));
    }

    public HttpResponse post(String url) {
        return request(HttpRequest.post(url));
    }

    public HttpResponse post(String url, Object body) {
        return request(HttpRequest.post(url, body));
    }

    public HttpResponse put(String url) {
        return request(HttpRequest.put(url));
    }

    public HttpResponse put(String url, Object body) {
        return request(HttpRequest.put(url, body));
    }

    public HttpResponse patch(String url) {
        return request(HttpRequest.patch(url));
    }

    public HttpResponse patch(String url, Object body) {
        return request(HttpRequest.patch(url, body));
    }

    public HttpResponse delete(String url) {
        return request(HttpRequest.delete(url));
    }

    public HttpResponse delete(String url, Object body) {
        return request(HttpRequest.delete(url, body));
    }


}
