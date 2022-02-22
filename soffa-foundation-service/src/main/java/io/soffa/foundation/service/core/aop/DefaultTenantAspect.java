package io.soffa.foundation.service.core.aop;

import io.soffa.foundation.annotations.DefaultTenant;
import io.soffa.foundation.core.context.RequestContextHolder;
import io.soffa.foundation.core.models.TenantId;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
@Order(DefaultTenantAspect.ORDER)
public class DefaultTenantAspect {

    public static final int ORDER = SecurityAspect.ORDER + 100;

    @SneakyThrows
    @Around("@annotation(arg)")
    public Object handle(ProceedingJoinPoint pjp, DefaultTenant arg) {
        final String currentTenant = RequestContextHolder.getTenant().orElse(null);
        try {
            RequestContextHolder.setTenant(TenantId.DEFAULT);
            return pjp.proceed(pjp.getArgs());
        } finally {
            RequestContextHolder.setTenant(currentTenant);
        }
    }

}
