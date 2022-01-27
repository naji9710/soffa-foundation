package io.soffa.foundation.actions;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.events.Event;

public interface ActionDispatcher {

    int SLOW_ACTION_THRESHOLD = 3;

    <I, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request, RequestContext context);

    <I, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request);

    <O> O dispatch(Class<? extends Action0<O>> actionClass, RequestContext context);

    <O> O dispatch(Class<? extends Action0<O>> actionClass);

    /**
     * Dispatch event to the right action handler base on the event name
     *
     * @param event The event to handle
     */
    void handle(Event event);


}
