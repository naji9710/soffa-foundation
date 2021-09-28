package io.soffa.foundation.service.actions;

import io.soffa.foundation.Action;
import io.soffa.foundation.RequestContext;

import javax.validation.Valid;

public interface ActionDispatcher {

    <I,O> O dispatch(Class<? extends Action<I, O>> actionClass, @Valid I request, RequestContext context);

    <I,O> O dispatch(Class<? extends Action<I, O>> actionClass, @Valid I request);

}
