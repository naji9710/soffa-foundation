package io.soffa.foundation.actions;

import io.soffa.foundation.core.Action;
import io.soffa.foundation.core.Action0;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.Validatable;
import io.soffa.foundation.pubsub.Event;

import javax.validation.Valid;

public interface ActionDispatcher {

    <I extends Validatable,O> O dispatch(Class<? extends Action<I, O>> actionClass, @Valid I request, RequestContext context);

    <I extends Validatable,O> O dispatch(Class<? extends Action<I, O>> actionClass, @Valid I request);

    <O> O dispatch(Class<? extends Action0<O>> actionClass, RequestContext context);

    <O> O dispatch(Class<? extends Action0<O>> actionClass);

    void handle(Event event) throws MissingEventHandlerException;


}
