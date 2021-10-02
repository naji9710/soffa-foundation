package io.soffa.foundation.spring;

import io.soffa.foundation.context.GrantedRole;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.model.Authentication;
import io.soffa.foundation.core.model.TenantId;
import io.soffa.foundation.exceptions.UnauthorizedException;
import io.soffa.foundation.jwt.JwtDecoder;
import io.soffa.foundation.lang.TextUtil;
import io.soffa.foundation.logging.Logger;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class RequestFilter extends OncePerRequestFilter {

    private final Logger logger = Logger.create(RequestFilter.class);
    private JwtDecoder jwtDecoder;

    public RequestFilter(@Autowired(required = false) JwtDecoder jwtDecoder) {
        super();
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain chain) throws ServletException, IOException {

        RequestContext context = new RequestContext();

        lookupHeader(request, "X-TenantId", "X-Tenant").ifPresent(value -> {
            logger.debug("Tenant found in context", value);
            context.setTenantId(new TenantId(value));
        });

        lookupHeader(request, "X-ApplicationName", "X-ApplicationId", "X-Application", "X-App").ifPresent(context::setApplicationName);

        lookupHeader(request, "X-TraceId", "X-CorrelationId").ifPresent(context::setTraceId);
        lookupHeader(request, "X-SpanId").ifPresent(context::setSpanId);
        lookupHeader(request, "X-RequestId").ifPresent(context::setRequestId);

        SecurityContextHolder.getContext().setAuthentication(
            new AnonymousAuthenticationToken("guest", context,
                Collections.singletonList(new SimpleGrantedAuthority("guest")))
        );

        if (jwtDecoder != null) {
            lookupHeader(request, HttpHeaders.AUTHORIZATION, "X-JWT-Assertion", "X-JWT-Assertions").ifPresent(value -> {
                String token = value.substring("bearer ".length()).trim();
                logger.debug("Bearer authorization header found: {}", token);
                Optional<Authentication> auth = jwtDecoder.decode(token);
                if (!auth.isPresent()) {
                    throw new UnauthorizedException("jwt.invalid");
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
                context.setAuthentication(auth.get());
                UsernamePasswordAuthenticationToken authz = new UsernamePasswordAuthenticationToken(context, null, permissions);
                SecurityContextHolder.getContext().setAuthentication(authz);
            });
        }

        if (context.getTenantId() != null) {
            TenantHolder.set(context.getTenantId().getValue());
        }

        RequestContextHolder.set(context);
        chain.doFilter(request, response);
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
