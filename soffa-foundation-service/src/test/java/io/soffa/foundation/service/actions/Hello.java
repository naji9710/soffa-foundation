package io.soffa.foundation.service.actions;

import io.soffa.foundation.annotations.Handle;
import io.soffa.foundation.api.Operation0;
import io.soffa.foundation.context.RequestContext;
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
