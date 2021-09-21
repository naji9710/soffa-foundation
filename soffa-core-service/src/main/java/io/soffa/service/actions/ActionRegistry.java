package io.soffa.service.actions;

import io.soffa.service.core.Action;

public interface ActionRegistry {

    <I, O> Action<I, O> lookup(Class<? extends Action<I,O>> action);

}
