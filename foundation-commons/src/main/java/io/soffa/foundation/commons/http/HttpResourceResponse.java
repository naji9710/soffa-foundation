package io.soffa.foundation.commons.http;

import io.soffa.foundation.commons.IOUtil;
import lombok.SneakyThrows;

import java.net.URL;

public class HttpResourceResponse implements HttpResponseProvider {

    private String contentType = "application/json";
    private final String location;


    @SneakyThrows
    public HttpResourceResponse(String location) {
        this.location = location;
        if (location.endsWith(".xml")) {
            contentType = "text/xml";
        }
    }

    @Override
    public HttpResponse apply(URL url, HttpHeaders headers) {
        return HttpResponse.ok(contentType, IOUtil.getResourceAsString(location));
    }
}
