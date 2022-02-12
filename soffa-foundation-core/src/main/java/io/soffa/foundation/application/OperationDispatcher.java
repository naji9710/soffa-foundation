package io.soffa.foundation.application;

import io.soffa.foundation.api.Operation;
import io.soffa.foundation.api.Operation0;
import io.soffa.foundation.context.RequestContext;

public interface OperationDispatcher {

    int SLOW_ACTION_THRESHOLD = 3;

    <I, O> O dispatch(Class<? extends Operation<I, O>> operationClass, I input, RequestContext context);

    <I, O> O dispatch(Class<? extends Operation<I, O>> operationClass, I input);

    <O> O dispatch(Class<? extends Operation0<O>> operationClass, RequestContext context);

    <O> O dispatch(Class<? extends Operation0<O>> operationClass);

}
