package io.soffa.foundation.service.aop;

import io.soffa.foundation.annotations.Journal;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.model.TenantId;
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
@Order(JournalAspect.ORDER)
public class JournalAspect {

    public static final int ORDER = DefaultTenantAspect.ORDER + 100;

    @SneakyThrows
    @Around("@annotation(arg)")
    public Object handle(ProceedingJoinPoint pjp, Journal arg) {
        TenantHolder.set(TenantId.DEFAULT);
        return pjp.proceed(pjp.getArgs());
    }

}
