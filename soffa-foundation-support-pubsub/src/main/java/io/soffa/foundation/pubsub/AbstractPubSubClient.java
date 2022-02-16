package io.soffa.foundation.pubsub;

import io.soffa.foundation.api.Operation;
import io.soffa.foundation.commons.ClassUtil;
import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.errors.ErrorUtil;
import io.soffa.foundation.model.CallResponse;
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
                RequestContext context = (RequestContext) args[1];
                Message msg = new Message(operationClass.getSimpleName(), args[0], context);
                CallResponse callResponse = request(subjet, msg, CallResponse.class).get(ASYNC_TIMEOUT_SECONDS.get(), TimeUnit.SECONDS);
                if (callResponse.isSuccess()) {
                    return ObjectUtil.deserialize(callResponse.getData(), returnType);
                } else {
                    throw ErrorUtil.getException(callResponse.getErrorCode(), callResponse.getError());
                }
            });
    }

}
