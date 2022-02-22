package io.soffa.foundation.core;


import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.http.HttpClient;
import io.soffa.foundation.commons.http.HttpRequest;
import io.soffa.foundation.commons.http.HttpResponse;
import io.soffa.foundation.core.openapi.ApiInfo;
import io.soffa.foundation.errors.ForbiddenException;
import io.soffa.foundation.errors.FunctionalException;
import io.soffa.foundation.errors.TechnicalException;
import io.soffa.foundation.errors.UnauthorizedException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public final class RestClient implements InvocationHandler {

    private final HttpClient client;
    private final String baseUrl;
    private final Map<String, ApiInfo> infos;

    private RestClient(HttpClient client, String baseUrl, Class<?> clientInterface) {
        this.client = client;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.infos = ApiInfo.of(clientInterface);
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clientInterface, String baseUrl) {
        return newInstance(clientInterface, baseUrl, HttpClient.getInstance());
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clientInterface, String baseUrl, HttpClient client) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[]{clientInterface},
            new RestClient(client, baseUrl, clientInterface));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object... args) throws Throwable {
        if ("hashCode".equals(method.getName())) {
            return baseUrl.hashCode();
        }
        if ("equals".equals(method.getName())) {
            return method.equals(args[0]);
        }
        HttpResponse response = this.client.request(createRequest(method, args));
        return parseResponse(response, method);
    }

    public Object parseResponse(HttpResponse response, Method method) {
        if (response.is2xxSuccessful()) {
            return JsonUtil.deserialize(response.getBody(), method.getReturnType());
        }
        if (response.isForbidden()) {
            throw new ForbiddenException(response.getMessageOrBody());
        }
        if (response.isUnauthorized()) {
            throw new UnauthorizedException(response.getMessageOrBody());
        }
        if (response.is4xxClientError()) {
            throw new FunctionalException(response.getMessageOrBody());
        }
        throw new TechnicalException(response.getMessageOrBody());
    }

    public HttpRequest createRequest(Method method, Object... args) {
        ApiInfo apiInfo = infos.get(method.getName());
        if (apiInfo == null) {
            throw new TechnicalException("Method not registered: %s", method.getName());
        }

        HttpRequest request = new HttpRequest(apiInfo.getMethod(), baseUrl + apiInfo.getPath());
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof RequestContext) {
                    RequestContext context = (RequestContext) arg;
                    request.setHeaders(context.getHeaders());
                } else {
                    request.setBody(arg);
                }
            }
        }
        return request;
    }


}
