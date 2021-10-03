package io.soffa.foundation.http;


import io.soffa.foundation.lang.TextUtil;
import lombok.SneakyThrows;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class HttpUtil {

    private static final List<Interceptor> INTERCEPTORS = new ArrayList<>();

    private HttpUtil() {}

    @SneakyThrows
    public static OkHttpClient newClientWithProxy(String proxy) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (TextUtil.isNotEmpty(proxy)) {
            URL parsedUrl = new URL(proxy);
            Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(parsedUrl.getHost(), parsedUrl.getPort()));
            clientBuilder.proxy(p);
        }
        for (Interceptor interceptor : INTERCEPTORS) {
            clientBuilder.addInterceptor(interceptor);
        }
        return clientBuilder.build();
    }

    public static void addInterceptor(Interceptor interceptor) {
        INTERCEPTORS.add(interceptor);
    }

}
