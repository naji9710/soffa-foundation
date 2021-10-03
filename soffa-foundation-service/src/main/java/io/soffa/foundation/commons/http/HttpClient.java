package io.soffa.foundation.commons.http;

import lombok.SneakyThrows;
import okhttp3.*;

public class HttpClient {

    private final OkHttpClient client;

    public HttpClient(OkHttpClient client) {
        this.client = client;
    }

    @SneakyThrows
    public HttpResponse get(String url) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        Call call = client.newCall(request);
        try(Response result = call.execute()) {
            HttpResponse.HttpResponseBuilder res = HttpResponse.builder()
                .status(result.code())
                .message(result.message());
            try(ResponseBody responseBody = result.body()) {
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
}
