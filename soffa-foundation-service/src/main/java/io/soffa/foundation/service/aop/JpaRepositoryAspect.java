package io.soffa.foundation.service.aop;

import io.soffa.foundation.commons.ErrorUtil;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.exceptions.DatabaseException;
import io.soffa.foundation.exceptions.ManagedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class JpaRepositoryAspect {


    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository*.*(..))")
    public Object catchJpaException(ProceedingJoinPoint jp) throws Throwable {
        try {
            return jp.proceed();
        } catch (Exception e) {
            if (e instanceof ManagedException) {
                throw e;
            } else {
                Throwable error = ErrorUtil.unwrap(e);
                String msg = error.getMessage().toLowerCase();
                if (msg.contains("table") && msg.contains("not found")) {
                    throw new DatabaseException(error.getMessage() + " -- make sure there is an active and valid Tenant (current: %s)", TenantHolder.get().orElse(null), e);
                }
                throw new DatabaseException(error.getMessage(), e);
            }
        }
    }

}
