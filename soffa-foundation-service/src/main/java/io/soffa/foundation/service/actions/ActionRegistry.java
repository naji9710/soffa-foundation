package io.soffa.foundation.service.actions;

import io.soffa.foundation.Action;

public interface ActionRegistry {

    <I, O> Action<I, O> lookup(Class<? extends Action<I,O>> action);

}
