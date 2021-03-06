package io.soffa.foundation.spring;

import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.model.Authentication;
import io.soffa.foundation.core.model.TenantId;
import io.soffa.foundation.security.AuthManager;
import io.soffa.foundation.security.roles.GrantedRole;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@NoArgsConstructor
public class RequestFilter extends OncePerRequestFilter {

    private static final Logger LOG = Logger.get(RequestFilter.class);
    private AuthManager authManager;

    public RequestFilter(@Autowired(required = false) AuthManager authManager) {
        super();
        this.authManager = authManager;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        RequestContext context = new RequestContext();

        if (TenantHolder.hasDefault) {
            context.setTenantId(new TenantId("default"));
        }

        lookupHeader(request, "X-TenantId", "X-Tenant").ifPresent(value -> {
            LOG.debug("Tenant found in context", value);
            context.setTenantId(new TenantId(value));
        });
        lookupHeader(request, "X-Application", "X-ApplicationName", "X-ApplicationId", "X-App").ifPresent(context::setApplicationName);
        lookupHeader(request, "X-TraceId", "X-Trace-Id", "X-RequestId", "X-Request-Id").ifPresent(context::setTraceId);
        lookupHeader(request, "X-SpanId", "X-Span-Id", "X-CorrelationId", "X-Correlation-Id").ifPresent(context::setSpanId);

        SecurityContextHolder.getContext().setAuthentication(
            new AnonymousAuthenticationToken("guest", context,
                Collections.singletonList(new SimpleGrantedAuthority("guest")))
        );
        processTracing(context);
        lookupHeader(request, HttpHeaders.AUTHORIZATION, "X-JWT-Assertion", "X-JWT-Assertions").ifPresent(value -> {
            if (authManager == null) {
                LOG.warn("Authorization header received but not authManager provided");
            } else {
                processAuthentication(context, value, response);
            }
        });
        try {
            RequestContextHolder.set(context);
            chain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
        }
    }

    private void processTracing(RequestContext context) {
        String prefix = "";
        if (context.getTenantId() != null) {
            TenantHolder.set(context.getTenantId().getValue());
            prefix = context.getTenantId().getValue() + "_";
            Logger.setTenantId(context.getTenantId());
        }

        if (TextUtil.isEmpty(context.getSpanId())) {
            context.setSpanId(IdGenerator.shortUUID(prefix));
        }
        if (TextUtil.isEmpty(context.getTraceId())) {
            context.setTraceId(IdGenerator.shortUUID(prefix));
        }
    }

    private void processAuthentication(RequestContext context, String value, HttpServletResponse response) {
        Optional<Authentication> auth = Optional.empty();

        if (value.toLowerCase().startsWith("bearer ")) {
            String token = value.substring("bearer ".length()).trim();
            LOG.debug("Bearer authorization header found: %s", token);
            auth = authManager.authenticate(context, token);
        } else if (value.toLowerCase().startsWith("basic ")) {
            String basicAuth = value.substring("basic ".length()).trim();
            String[] credentials = new String(Base64.getDecoder().decode(basicAuth)).split(":");
            String username = credentials[0];
            String pasword = "";
            boolean hasPassword = credentials.length > 1;
            if (hasPassword) {
                pasword = credentials[1];
            }
            auth = authManager.authenticate(context, username, pasword);
        }

        if (!auth.isPresent()) {
            try {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            } catch (IOException e) {
                LOG.error(e, e.getMessage());
            }
            return;
        }
        List<GrantedAuthority> permissions = new ArrayList<>();
        permissions.add(new SimpleGrantedAuthority(GrantedRole.USER));
        permissions.add(new SimpleGrantedAuthority(GrantedRole.AUTHENTICATED));
        if (TextUtil.isNotEmpty(context.getApplicationName())) {
            permissions.add(new SimpleGrantedAuthority(GrantedRole.HAS_APPLICATION));
        }
        if (context.getTenantId() != null) {
            permissions.add(new SimpleGrantedAuthority(GrantedRole.HAS_TENANT_ID));
        }
        if (auth.get().getRoles() != null) {
            for (String role : auth.get().getRoles()) {
                permissions.add(new SimpleGrantedAuthority(role));
            }
        }
        if (auth.get().getPermissions() != null) {
            for (String permission : auth.get().getPermissions()) {
                permissions.add(new SimpleGrantedAuthority(permission));
            }
        }
        context.setAuthentication(auth.get());
        context.setAuthorization(value);
        UsernamePasswordAuthenticationToken authz = new UsernamePasswordAuthenticationToken(context, null, permissions);
        SecurityContextHolder.getContext().setAuthentication(authz);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = "/" + request.getRequestURI().split("\\?")[0].replaceAll("^/|/$", "".toLowerCase());
        uri = uri.replace(request.getContextPath(), "");
        if (!uri.startsWith("/")) {
            uri = "/";
        }
        boolean isStaticResourceRequest = uri.matches(".*\\.(css|js|ts|html|htm|map|g?zip|gz|ico|png|gif|svg|woff|ttf|eot|jpe?g2?)$");
        if (isStaticResourceRequest) {
            return true;
        }
        boolean isOpenAPIRequest = uri.matches("/swagger.*") || uri.matches("/v3/api-docs/?.*?");
        if (isOpenAPIRequest) {
            return true;
        }
        return uri.matches("/actuator/.*|/healthz");
    }

    private Optional<String> lookupHeader(HttpServletRequest request, String... candidates) {
        for (String candidate : candidates) {
            String header = request.getHeader(candidate);
            if (header != null && !header.isEmpty()) {
                return Optional.of(header.trim());
            }
        }
        return Optional.empty();
    }

}
