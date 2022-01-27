package io.soffa.foundation.app.core;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.model.TenantId;
import io.soffa.foundation.exceptions.FakeException;
import org.jetbrains.annotations.NotNull;

import javax.inject.Named;


@Named
public class PingActionImpl implements PingAction {

    public static final TenantId T2 = new TenantId("T2");

    @Override
    public PingResponse handle(@NotNull RequestContext context) {
        if (T2.equals(context.getTenantId())) {
            throw new FakeException("Controlled error triggered (%s)", context.getTenantId());
        } else {
            return new PingResponse("PONG");
        }
    }

}
