package io.soffa.foundation.service.aop;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.errors.UnauthorizedException;
import io.soffa.foundation.errors.ValidationException;
import io.soffa.foundation.context.RequestContext;
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

    private static final Logger LOG = Logger.get(SecurityAspect.class);
    private static final Throwable ERR_AUTH_REQUIRED = new UnauthorizedException("Authentication is required to access this resource.");
    private static final Throwable ERR_APP_REQUIRED = new ValidationException("An ApplicationName is required to access this resource.");
    private static final Throwable ERR_TENANT_REQUIRED = new ValidationException("A TenantId is required to access this resource.");

    @SneakyThrows
    @Before("@within(io.soffa.foundation.annotations.Authenticated) || @annotation(io.soffa.foundation.annotations.Authenticated)")
    public void checkAuthenticated(JoinPoint point) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            LOG.warn("Access denied to [%s.%s], current context does not contain an authentication",point.getSignature().getDeclaringTypeName(),point.getSignature().getName());
            throw ERR_AUTH_REQUIRED;
        }
    }

    @SneakyThrows
    @Before("@within(io.soffa.foundation.annotations.ApplicationRequired) || @annotation(io.soffa.foundation.annotations.ApplicationRequired)")
    public void checkApplication(JoinPoint point) {
        RequestContext context = getRequestContext().orElseThrow(() -> ERR_APP_REQUIRED);
        if (TextUtil.isEmpty(context.getApplicationName())) {
            LOG.warn("Access denied to [%s.%s], current context does not contain a valid applicationName", point.getSignature().getDeclaringTypeName(),point.getSignature().getName());
            throw ERR_APP_REQUIRED;
        }
    }

    @SneakyThrows
    @Before("@within(io.soffa.foundation.annotations.TenantRequired) || @annotation(io.soffa.foundation.annotations.TenantRequired)")
    public void checkTenant(JoinPoint point) {
        RequestContext context = getRequestContext().orElseThrow(() -> ERR_TENANT_REQUIRED);
        if (context.getTenantId() == null) {
            LOG.warn("Access denied to [%s.%s], current context does not contain a valid tenant", point.getSignature().getDeclaringTypeName(), point.getSignature().getName());
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


}
