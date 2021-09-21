package io.soffa.service.actions;

import io.soffa.service.core.Action;
import io.soffa.service.core.RequestContext;

import javax.validation.Valid;

public interface ActionDispatcher {

    <I,O> O dispatch(Class<? extends Action<I, O>> actionClass, @Valid I request, RequestContext context);

    <I,O> O dispatch(Class<? extends Action<I, O>> actionClass, @Valid I request);

}
