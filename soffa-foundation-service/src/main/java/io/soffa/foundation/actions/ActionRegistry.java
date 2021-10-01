package io.soffa.foundation.actions;

import io.soffa.foundation.core.Action;
import io.soffa.foundation.core.Validatable;

public interface ActionRegistry {

    <I extends Validatable, O> Action<I, O> lookup(Class<? extends Action<I,O>> action);

}
