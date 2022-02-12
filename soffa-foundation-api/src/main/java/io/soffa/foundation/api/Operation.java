package io.soffa.foundation.api;

import io.soffa.foundation.context.RequestContext;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @param <I>
 * @param <O>
 */
public interface Operation<I, O> {

    O handle(@NonNull I input, @NonNull RequestContext context);

}
