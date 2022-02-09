package io.soffa.foundation.core.actions;

import io.soffa.foundation.core.RequestContext;

public interface ActionDispatcher {

    int SLOW_ACTION_THRESHOLD = 3;

    <I, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request, RequestContext context);

    <I, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request);

    <O> O dispatch(Class<? extends Action0<O>> actionClass, RequestContext context);

    <O> O dispatch(Class<? extends Action0<O>> actionClass);


}
