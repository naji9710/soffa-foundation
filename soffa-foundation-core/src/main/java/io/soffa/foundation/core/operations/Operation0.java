package io.soffa.foundation.core.operations;

import io.soffa.foundation.core.RequestContext;
import org.checkerframework.checker.nullness.qual.NonNull;


/**
 * @param <O>
 */
public interface Operation0<O> {

    O handle(@NonNull RequestContext context);

}
