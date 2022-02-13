package io.soffa.foundation.messages;


import io.soffa.foundation.annotations.BindOperation;
import io.soffa.foundation.commons.ClassUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.exceptions.ConfigurationException;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.security.AuthUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface BinaryClient extends MessageDispatcher {

    Logger LOG = Logger.get(BinaryClient.class);

    void subsribe(String subject, String queue, MessageHandler handler);

    CompletableFuture<byte[]> request(String subject, Message event);

    <T> CompletableFuture<T> request(String subject, Message event, Class<T> expectedClass);

    void publish(String subject, Message message);

    String getServiceToken();

    default <I,O> CompletableFuture<O> requestWithServiceToken(String subject, Class<? extends io.soffa.foundation.api.Operation<I,O>> operation, I input) {
        return requestWithServiceToken(subject, operation, input, RequestContextHolder.getOrCreate());
    }

    default <I,O> CompletableFuture<O> requestWithServiceToken(String subject, Class<? extends io.soffa.foundation.api.Operation<I,O>> operation, I input, RequestContext context) {
        String serviceToken = getServiceToken();
        if (TextUtil.isEmpty(serviceToken)) {
            throw new ConfigurationException("serviceToken is not configured");
        }
        return request(subject, operation, input, context.withAuthorization(AuthUtil.createBasicAuthorization(context.getServiceName(), serviceToken)));
    }

    default <I,O> CompletableFuture<O> request(String subject, Class<? extends io.soffa.foundation.api.Operation<I,O>> operation, I input) {
        return request(subject, operation, input, RequestContextHolder.getOrCreate());
    }

    default <I,O> CompletableFuture<O> request(String subject, Class<? extends io.soffa.foundation.api.Operation<I,O>> operation, Object input, RequestContext context) {
        Message message = new Message(
            operation.getSimpleName(),
            input,
            context
        );
        @SuppressWarnings("unchecked")
        Class<O> returnType = (Class<O>) ClassUtil.getClassFromGenericInterface(operation, io.soffa.foundation.api.Operation.class, 1);
        try {
            return request(subject, message, returnType);
        } catch (Exception e) {
            throw new TechnicalException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    default <T> T createClient(Class<T> clientInterface, String subject) {

        Map<Method, String> mapping = new HashMap<>();

        for (Method method : clientInterface.getDeclaredMethods()) {
            BindOperation binding = method.getAnnotation(BindOperation.class);
            if (binding != null) {
                mapping.put(method, binding.value().getName());
            }
        }

        if (mapping.isEmpty()) {
            throw new TechnicalException("No method found with annotation @BindOperation");
        }

        return (T) java.lang.reflect.Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[]{clientInterface},
            (proxy, method, args) -> {
                if ("hashCode".equals(method.getName())) {
                    return clientInterface.getName().hashCode();
                }
                if ("equals".equals(method.getName())) {
                    return method.equals(args[0]);
                }
                if (!mapping.containsKey(method)) {
                    throw new TechnicalException("This method has no @BindOperation annotation");
                }
                return request(subject, createMessage(mapping.get(method), args), method.getReturnType()).get(30, TimeUnit.SECONDS);
            });
    }

    default Message createMessage(String operation, Object... args) {
        RequestContext context = null;
        Object input = null;
        for (Object arg : args) {
            if (arg instanceof RequestContext) {
                if (context == null) {
                    context = (RequestContext) arg;
                }
            } else if (input == null) {
                input = arg;
            }
            if (input != null && context != null) {
                break;
            }
        }
        if (context == null) {
            context = RequestContextHolder.get().orElseGet(RequestContext::new);
        }
        return new Message(operation, input, context);
    }


}


