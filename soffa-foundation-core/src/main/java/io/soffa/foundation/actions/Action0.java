package io.soffa.foundation.actions;

import io.soffa.foundation.core.RequestContext;
import org.checkerframework.checker.nullness.qual.NonNull;


/**
 * @param <O>
 */
public interface Action0<O> {

    O handle(@NonNull RequestContext context);

}
