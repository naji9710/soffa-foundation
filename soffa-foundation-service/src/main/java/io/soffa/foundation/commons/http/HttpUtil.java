package io.soffa.foundation.commons.http;


import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Regex;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.lang.TextUtil;
import lombok.SneakyThrows;
import okhttp3.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class HttpUtil {

    private static final List<Interceptor> INTERCEPTORS = new ArrayList<>();

    private HttpUtil() {
    }

    @SneakyThrows
    public static OkHttpClient newOkHttpClient() {
        return newOkHttpClient(null);
    }

    @SneakyThrows
    public static OkHttpClient newOkHttpClient(String proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .proxy(Proxy.NO_PROXY)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true);

        if (TextUtil.isNotEmpty(proxy)) {
            URL parsedUrl = new URL(proxy);
            Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(parsedUrl.getHost(), parsedUrl.getPort()));
            String userInfo = parsedUrl.getUserInfo();
            builder.proxy(p);
            if (TextUtil.isNotEmpty(userInfo)) {
                String[] parts = userInfo.split(":");
                String credential = Credentials.basic(parts[0], parts[1]);
                builder.proxyAuthenticator((route, response) -> {
                    // Emty Line
                    return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
                });
            }
        }

        builder.addNetworkInterceptor(chain -> {
            Request originalRequest = chain.request();
            String contentType = originalRequest.header("Content-Type");
            if (TextUtil.isEmpty(contentType) || contentType.contains("application/json")) {
                contentType = "application/json"; // Because OkHttp adds ;charset-utf8
            }
            Request.Builder request = originalRequest.newBuilder().header("Content-Type", contentType);
            RequestContextHolder.get().ifPresent(context -> {
                Map<String, String> headers = ImmutableMap.of(
                    "X-TenantId", context.getTenant(),
                    "X-TraceId", context.getTraceId(),
                    "X-SpanId", context.getSpanId(),
                    "X-Application", context.getSpanId()
                );
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    boolean isHeaderMissing = TextUtil.isEmpty(originalRequest.header(e.getKey()));
                    if (isHeaderMissing && TextUtil.isNotEmpty(e.getValue())) {
                        request.header(e.getKey(), e.getValue());
                    }
                }
            });
            return chain.proceed(request.build());
        });


        for (Interceptor interceptor : INTERCEPTORS) {
            builder.addInterceptor(interceptor);
        }
        return builder.build();
    }

    public static void mockResponse(String host, String path, HttpResponseProvider handler) {
        mockResponse(host, path, false, handler);
    }

    public static void mockResponse(String host, Regex path, HttpResponseProvider handler) {
        mockResponse(host, path.getValue(), true, handler);
    }

    private static void mockResponse(String host, String path, boolean regex, HttpResponseProvider handler) {
        addInterceptor(chain -> {
            Request request = chain.request();
            String encodedPath = request.url().encodedPath();
            if (!host.equalsIgnoreCase(request.url().host())) {
                return chain.proceed(request);
            }
            if (regex && !encodedPath.matches(path)) {
                return chain.proceed(request);
            }
            if (!regex && !encodedPath.equals(path)) {
                return chain.proceed(request);
            }
            HttpResponse res = handler.apply(request.url().url());
            MediaType contentType = MediaType.parse(res.getContentType());
            return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(res.getStatus())
                .message("Mocked response")
                .body(ResponseBody.create(JsonUtil.serialize(res.getBody()), contentType))
                .build();

        });
    }

    public static void addInterceptor(Interceptor interceptor) {
        INTERCEPTORS.add(interceptor);
    }

}
