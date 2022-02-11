package io.soffa.foundation.spring.actions;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.annotations.Handle;
import io.soffa.foundation.core.operations.Operation0;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Handle("HELLO")
public class Hello implements Operation0<String> {

    public static final AtomicInteger TICK = new AtomicInteger(0);

    @Override
    public String handle(@NonNull RequestContext context) {
        TICK.incrementAndGet();
        return null;
    }
}
