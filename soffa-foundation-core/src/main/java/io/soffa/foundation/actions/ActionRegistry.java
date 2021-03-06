package io.soffa.foundation.actions;

import io.soffa.foundation.core.model.Validatable;

public interface ActionRegistry {

    <I extends Validatable, O> Action<I, O> lookup(Class<? extends Action<I, O>> action);

}
