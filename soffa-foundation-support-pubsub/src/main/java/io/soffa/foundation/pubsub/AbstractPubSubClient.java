package io.soffa.foundation.pubsub;

import io.soffa.foundation.api.HttpStatus;
import io.soffa.foundation.api.Operation;
import io.soffa.foundation.commons.ClassUtil;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.errors.*;
import io.soffa.foundation.model.Message;
import io.soffa.foundation.model.OperationResult;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;
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
                return request(subjet, msg, returnType).get(ASYNC_TIMEOUT_SECONDS.get(), TimeUnit.SECONDS);
            });
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> CompletableFuture<T> request(@NonNull String subject, Message message, final Class<T> responseClass) {
        return internalRequest(subject, message).thenApply(data -> unwrapResponse(data, responseClass));
    }

    public abstract CompletableFuture<byte[]> internalRequest(@NonNull String subject, Message message);

    public <T> T unwrapResponse(byte[] data, final Class<T> responseClass) {
        OperationResult response = ObjectUtil.deserialize(data, OperationResult.class);
        if (response.isSuccess()) {
            return JsonUtil.deserialize(response.getData(), responseClass);
        } else {
            switch (response.getErrorCode()) {
                case HttpStatus.UNAUTHORIZED:
                    throw new UnauthorizedException(response.getError());
                case HttpStatus.FORBIDDEN:
                    throw new ForbiddenException(response.getError());
                case HttpStatus.BAD_REQUEST:
                    throw new FunctionalException(response.getError());
                default:
                    throw new TechnicalException(response.getError());
            }
        }
    }
}
