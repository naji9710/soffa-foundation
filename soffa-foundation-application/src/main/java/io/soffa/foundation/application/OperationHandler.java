package io.soffa.foundation.application;

import io.soffa.foundation.api.Operation;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextHolder;

public interface OperationHandler {

    <I, O> O handle(Class<? extends Operation<I, O>> operationClass, I input, RequestContext context);

    <I, O> O handle(Class<? extends Operation<I, O>> operationClass, I input);

    default <I extends Void, O> O handle(Class<? extends Operation<I, O>> operationClass) {
        return handle(operationClass, null, RequestContextHolder.require());
    }

    default <I extends Void, O> O handle(Class<? extends Operation<I, O>> operationClass, RequestContext context) {
        return handle(operationClass, null, context);
    }



}
