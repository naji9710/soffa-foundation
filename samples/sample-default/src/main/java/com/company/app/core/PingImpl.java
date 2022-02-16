package com.company.app.core;

import io.soffa.foundation.errors.FakeException;
import io.soffa.foundation.context.RequestContext;
import org.jetbrains.annotations.NotNull;

import javax.inject.Named;


@Named
public class PingImpl implements Ping {

    public static final String T2 = "T2";

    @Override
    public PingResponse handle(Void arg, @NotNull RequestContext context) {
        if (T2.equals(context.getTenantId())) {
            throw new FakeException("Controlled error triggered (%s)", context.getTenantId());
        } else {
            return new PingResponse("PONG");
        }
    }

}
