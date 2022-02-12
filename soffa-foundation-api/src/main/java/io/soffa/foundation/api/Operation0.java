package io.soffa.foundation.api;

import io.soffa.foundation.context.RequestContext;
import org.checkerframework.checker.nullness.qual.NonNull;


/**
 * @param <O>
 */
public interface Operation0<O> {

    O handle(@NonNull RequestContext context);

}
