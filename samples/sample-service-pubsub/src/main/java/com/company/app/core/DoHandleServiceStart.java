package com.company.app.core;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.events.OnServiceStarted;
import io.soffa.foundation.core.events.ServiceInfo;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Named;
import java.util.concurrent.atomic.AtomicBoolean;

@Named
public class DoHandleServiceStart implements OnServiceStarted {

    public static final AtomicBoolean RECEIVED = new AtomicBoolean(false);

    @Override
    public Void handle(ServiceInfo input, @NonNull RequestContext context) {
        RECEIVED.set(true);
        return null;
    }

}
