package io.soffa.foundation.commons.http;

import io.soffa.foundation.commons.IOUtil;
import lombok.SneakyThrows;

import java.net.URL;

public class HttpResponseResource implements HttpResponseProvider {

    private final String content;
    private String contentType = "application/json";


    @SneakyThrows
    public HttpResponseResource(String location) {
        if (location.endsWith(".xml")) {
            contentType = "text/xml";
        }
        this.content = IOUtil.getResourceAsString(location);
    }

    public String getContent() {
        return content;
    }

    @Override
    public HttpResponse apply(URL url) {
        return HttpResponse.ok(contentType, content);
    }
}
