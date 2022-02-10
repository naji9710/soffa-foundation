package io.soffa.foundation.actions;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.actions.Action0;
import io.soffa.foundation.core.annotations.BindAction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@BindAction(name = "HELLO")
public class HelloActionHandler implements Action0<String> {

    public static final AtomicInteger TICK = new AtomicInteger(0);

    @Override
    public String handle(@NonNull RequestContext context) {
        TICK.incrementAndGet();
        return null;
    }
}
