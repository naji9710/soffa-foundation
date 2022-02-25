package io.soffa.foundation.core;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface Operation<I, O> {

    O handle(I input, @NonNull RequestContext context);

    default O handle(@NonNull RequestContext context) {
        return handle(null, context);
    }

}
