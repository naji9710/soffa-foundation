package io.soffa.foundation.service.core.aop;

import io.soffa.foundation.annotations.DefaultTenant;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.context.TenantContextHolder;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Aspect
@Component
@AllArgsConstructor
@Order(DefaultTenantAspect.ORDER)
public class DefaultTenantAspect {

    public static final int ORDER = 10;
    private static final Logger LOG = Logger.get(DefaultTenantAspect.class);

    @SneakyThrows
    @Around("@annotation(arg)")
    public Object handle(ProceedingJoinPoint pjp, DefaultTenant arg) {
        LOG.debug("@DefaultTenant enabled on %s:%s", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName());
        //noinspection Convert2Lambda
        return TenantContextHolder.useDefault(new Supplier<Object>() {
            @SneakyThrows
            @Override
            public Object get() {
                return pjp.proceed(pjp.getArgs());
            }
        });
    }

}
