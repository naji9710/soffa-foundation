package io.soffa.foundation.service.core.aop;

import io.soffa.foundation.annotations.Journal;
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
        return pjp.proceed(pjp.getArgs());
    }

}
