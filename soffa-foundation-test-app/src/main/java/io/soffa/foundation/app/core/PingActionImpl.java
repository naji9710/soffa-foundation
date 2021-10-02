package io.soffa.foundation.app.core;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.model.TenantId;
import io.soffa.foundation.exceptions.FakeException;

import javax.inject.Named;


@Named
public class PingActionImpl implements PingAction {

    public static final TenantId T1 = new TenantId("T1");

    @Override
    public String handle(RequestContext context) {
        if (!context.hasTenant() || context.getTenantId().equals(T1)) {
            return "PONG";
        } else {
            throw new FakeException("Controlled error triggered");
        }
    }

}
