package io.soffa.foundation.core.messages;


import io.soffa.foundation.commons.ClassUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.actions.Action0;
import io.soffa.foundation.core.actions.MessageHandler;
import io.soffa.foundation.core.annotations.ActionBind;
import io.soffa.foundation.exceptions.TechnicalException;

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

    default <O> CompletableFuture<O> request(String subject, Class<? extends Action0<O>> action) {
        Message message = new Message(
            action.getSimpleName(),
            action, RequestContextHolder.get().orElseGet(RequestContext::new)
        );
        @SuppressWarnings("unchecked")
        Class<O> returnType = (Class<O>) ClassUtil.getClassFromGenericInterface(action, Action0.class, 0);
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
            ActionBind binding = method.getAnnotation(ActionBind.class);
            if (binding != null) {
                LOG.debug("ActionBind %S found on method %s", binding.value(), method.getName());
                mapping.put(method, binding.value().getName());
            }
        }

        if (mapping.isEmpty()) {
            throw new TechnicalException("No method found with annotation @ActionBind");
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
                    throw new TechnicalException("This method has no @ActionBind annotation");
                }
                return request(subject, createMessage(mapping.get(method), args), method.getReturnType()).get(30, TimeUnit.SECONDS);
            });
    }

    default Message createMessage(String actionName, Object... args) {
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
        return new Message(actionName, input, context);
    }


}


