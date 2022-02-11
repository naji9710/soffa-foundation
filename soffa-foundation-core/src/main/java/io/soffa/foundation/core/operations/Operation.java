package io.soffa.foundation.core.operations;

import io.soffa.foundation.core.RequestContext;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @param <I>
 * @param <O>
 */
public interface Operation<I, O> {

    O handle(@NonNull I input, @NonNull RequestContext context);

}
