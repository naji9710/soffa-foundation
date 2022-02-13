package io.soffa.foundation.api;

import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextHolder;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @param <I>
 * @param <O>
 */
public interface Operation<I, O> {

    Void NO_INPUT = null;

    O handle(I input, @NonNull RequestContext context);

    default O handle(I input) {
        return handle(input, RequestContextHolder.require());
    }

}
