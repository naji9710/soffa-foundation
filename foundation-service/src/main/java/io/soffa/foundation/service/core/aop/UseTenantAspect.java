package io.soffa.foundation.service.core.aop;

import io.soffa.foundation.annotations.WithTenantContext;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.context.TenantContextHolder;
import io.soffa.foundation.core.models.TenantId;
import io.soffa.foundation.errors.InvalidTenantException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Aspect
@Component
@AllArgsConstructor
public class UseTenantAspect {

    private static final Logger LOG = Logger.get(UseTenantAspect.class);

    @SneakyThrows
    @Around("@annotation(arg)")
    public Object handle(ProceedingJoinPoint pjp, WithTenantContext arg) {
        String tenant = TenantContextHolder.get().orElse(null);
        if (pjp.getArgs() != null) {
            for (Object a : pjp.getArgs()) {
                if (a instanceof RequestContext) {
                    tenant = ((RequestContext) a).getTenantId();
                    break;
                }
            }
        }

        if (TextUtil.isEmpty(tenant) || TenantId.DEFAULT_VALUE.equals(tenant)) {
            throw new InvalidTenantException("No valid (non default) tenant found in current context");
        }

        LOG.debug("@DefaultTenant enabled on %s:%s", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName());
        //noinspection Convert2Lambda
        return TenantContextHolder.use(TenantId.of(tenant), new Supplier<Object>() {
            @SneakyThrows
            @Override
            public Object get() {
                return pjp.proceed(pjp.getArgs());
            }
        });
    }

}
