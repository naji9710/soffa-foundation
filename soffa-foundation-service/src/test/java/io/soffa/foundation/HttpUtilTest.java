package io.soffa.foundation;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.commons.Regex;
import io.soffa.foundation.commons.http.HttpClient;
import io.soffa.foundation.commons.http.HttpResponse;
import io.soffa.foundation.commons.http.HttpUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpUtilTest {

    @SneakyThrows
    @Test
    public void testInterceptor() {

        HttpUtil.mockResponse("devbox.local", "/", path -> {
            // Empty Line
            return HttpResponse.ok("text/plain", "PONG");
        });
        HttpClient client = new HttpClient(HttpUtil.newOkHttpClient());
        HttpResponse res = client.get("https://devbox.local");
        assertEquals(200, res.getStatus());
        assertEquals("text/plain", res.getContentType());
        assertEquals("PONG", res.getBody());

        HttpUtil.mockResponse("www.github.com", new Regex("/hello"), path -> {
            // Empty Line
            return HttpResponse.ok("application/json", ImmutableMap.of("message", "Hi"));
        });
        client = new HttpClient(HttpUtil.newOkHttpClient());
        res = client.get("https://www.github.com/hello");
        assertEquals(200, res.getStatus());
        assertEquals("application/json", res.getContentType());
        assertTrue(res.getBody().contains("Hi"));
    }

}
