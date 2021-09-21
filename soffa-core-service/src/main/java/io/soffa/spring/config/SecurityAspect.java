package io.soffa.spring.config;

import io.soffa.commons.exceptions.UnauthorizedException;
import io.soffa.commons.exceptions.ValidationException;
import io.soffa.commons.lang.TextUtil;
import io.soffa.service.core.RequestContext;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class SecurityAspect {

    @SneakyThrows
    @Before("@within(io.soffa.commons.annotations.Authenticated) || @annotation(io.soffa.commons.annotations.Authenticated)")
    public void checkAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw ERR_AUTH_REQUIRED;
        }
    }

    @SneakyThrows
    @Before("@within(io.soffa.commons.annotations.ApplicationRequired) || @annotation(io.soffa.commons.annotations.ApplicationRequired)")
    public void checkApplication() {
        RequestContext context = getRequestContext().orElseThrow(() -> ERR_APP_REQUIRED);
        if (TextUtil.isEmpty(context.getApplicationName())) {
            throw ERR_APP_REQUIRED;
        }
    }

    @SneakyThrows
    @Before("@within(io.soffa.commons.annotations.ApplicationRequired) || @annotation(io.soffa.commons.annotations.ApplicationRequired)")
    public void checkTenant() {
        RequestContext context = getRequestContext().orElseThrow(() -> ERR_TENANT_REQUIRED);
        if (context.getTenantId()==null) {
            throw ERR_TENANT_REQUIRED;
        }
    }

    private Optional<RequestContext> getRequestContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null && auth.getPrincipal() instanceof RequestContext) {
            return Optional.of((RequestContext) auth.getPrincipal());
        }
        return Optional.empty();
    }


    private static final Throwable ERR_AUTH_REQUIRED = new UnauthorizedException("Authentication is required to access this resource.");
    private static final Throwable ERR_APP_REQUIRED = new ValidationException("An ApplicationName is required to access this resource.");
    private static final Throwable ERR_TENANT_REQUIRED = new ValidationException("A TenantId is required to access this resource.");

}
