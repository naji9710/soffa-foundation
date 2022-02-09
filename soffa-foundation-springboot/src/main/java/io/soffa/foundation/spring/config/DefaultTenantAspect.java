package io.soffa.foundation.spring.config;

import io.soffa.foundation.annotations.DefaultTenant;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.core.data.SysLogRepository;
import io.soffa.foundation.core.model.TenantId;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnBean(SysLogRepository.class)
@AllArgsConstructor
public class DefaultTenantAspect {

    @SneakyThrows
    @Around("@annotation(arg)")
    public Object traceAction(ProceedingJoinPoint pjp, DefaultTenant arg) {
        final String currentTenant = TenantHolder.get().orElse(null);
        try {
            TenantHolder.set(TenantId.DEFAULT);
            return pjp.proceed(pjp.getArgs());
        } finally {
            TenantHolder.set(currentTenant);
        }

    }



}
