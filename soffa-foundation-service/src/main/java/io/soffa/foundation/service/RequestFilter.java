package io.soffa.foundation.service;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.RequestContextUtil;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.exceptions.InvalidAuthException;
import io.soffa.foundation.exceptions.InvalidTokenException;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.model.TenantId;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static io.soffa.foundation.metrics.CoreMetrics.HTTP_REQUEST;

@NoArgsConstructor
public class RequestFilter extends OncePerRequestFilter {

    private static final Logger LOG = Logger.get(RequestFilter.class);
    private PlatformAuthManager authManager;
    private MetricsRegistry metricsRegistry;

    public RequestFilter(PlatformAuthManager authManager,
                         MetricsRegistry metricsRegistry) {
        super();
        this.authManager = authManager;
        this.metricsRegistry = metricsRegistry;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) {

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
        AtomicBoolean proceed = new AtomicBoolean(true);
        lookupHeader(request, HttpHeaders.AUTHORIZATION, "X-JWT-Assertion", "X-JWT-Assertions").ifPresent(new Consumer<String>() {
            @SneakyThrows
            @Override
            public void accept(String value) {
                try {
                    authManager.process(context, value);
                }catch (InvalidAuthException | InvalidTokenException e) {
                    proceed.set(false);
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
                }catch (Exception e) {
                    proceed.set(false);
                    LOG.error(e);
                    response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
                }
            }
        });

        if (!proceed.get()) {
            return;
        }

        //noinspection Convert2Lambda
        metricsRegistry.timed(HTTP_REQUEST, RequestContextUtil.tagify(context, ImmutableMap.of("uri", request.getRequestURI())),
            new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    try {
                        RequestContextHolder.set(context);
                        chain.doFilter(request, response);
                    } finally {
                        RequestContextHolder.clear();
                    }
                }
            });
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
