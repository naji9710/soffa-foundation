package app.core;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.model.TenantId;
import io.soffa.foundation.exceptions.FunctionalException;

import javax.inject.Named;

import static app.Globals.logger;


@Named
public class PingActionImpl implements PingAction {

    public static final TenantId T1 = new TenantId("T1");

    @Override
    public String handle(RequestContext context) {
        logger.info("This is a sample log to showcase MDC fields");
        if (!context.hasTenant() || context.getTenantId().equals(T1)) {
            return "PONG";
        } else {
            throw new FunctionalException("Controlled error triggered");
        }
    }

}
