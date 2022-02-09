package io.soffa.foundation.core.actions;

import io.soffa.foundation.core.RequestContext;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @param <I>
 */
public interface Action1<I> {

    void handle(@NonNull I input, @NonNull RequestContext context);

}
