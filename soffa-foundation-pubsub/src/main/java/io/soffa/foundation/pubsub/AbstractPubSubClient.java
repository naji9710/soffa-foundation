package io.soffa.foundation.pubsub;

import io.soffa.foundation.api.Operation;
import io.soffa.foundation.commons.ClassUtil;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.model.Message;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPubSubClient implements PubSubClient {

    @SuppressWarnings("unchecked")
    @Override
    public final <I, O, T extends Operation<I, O>> T proxy(@NonNull String subjet, @NotNull Class<T> operationClass) {
        Class<?> returnType = ClassUtil.getClassFromGenericInterface(operationClass, Operation.class, 1);
        return (T) java.lang.reflect.Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[]{operationClass},
            (proxy, method, args) -> {
                if ("hashCode".equals(method.getName())) {
                    return operationClass.getName().hashCode();
                }
                if ("equals".equals(method.getName())) {
                    return method.equals(args[0]);
                }
                Object payload = args[0];
                String payloadType = (payload != null) ? payload.getClass().getName() : null;
                RequestContext context = (RequestContext) args[1];
                Message msg = new Message(
                    IdGenerator.shortUUID("msg"),
                    operationClass.getSimpleName(),
                    ObjectUtil.serialize(args[0]),
                    payloadType,
                    context.getHeaders()
                );
                return request(subjet, msg, returnType).get(ASYNC_TIMEOUT_SECONDS.get(), TimeUnit.SECONDS);
            });
    }

}
