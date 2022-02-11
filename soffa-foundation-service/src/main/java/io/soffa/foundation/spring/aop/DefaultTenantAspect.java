package io.soffa.foundation.spring.aop;

import io.soffa.foundation.core.annotations.DefaultTenant;
import io.soffa.foundation.core.context.TenantHolder;
import io.soffa.foundation.core.model.TenantId;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
public class DefaultTenantAspect {

    @SneakyThrows
    @Around("@annotation(arg)")
    public Object handle(ProceedingJoinPoint pjp, DefaultTenant arg) {
        final String currentTenant = TenantHolder.get().orElse(null);
        try {
            TenantHolder.set(TenantId.DEFAULT);
            return pjp.proceed(pjp.getArgs());
        } finally {
            TenantHolder.set(currentTenant);
        }

    }



}
