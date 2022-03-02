package io.soffa.foundation.service.core;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.context.DefaultRequestContext;
import io.soffa.foundation.core.context.RequestContextHolder;
import io.soffa.foundation.core.context.TenantContextHolder;
import io.soffa.foundation.core.security.PlatformAuthManager;
import io.soffa.foundation.errors.ErrorUtil;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@NoArgsConstructor
public class RequestFilter extends OncePerRequestFilter {

    private static final Logger LOG = Logger.get(RequestFilter.class);
    private PlatformAuthManager authManager;

    public RequestFilter(PlatformAuthManager authManager) {
        super();
        this.authManager = authManager;
        // this.metricsRegistry = metricsRegistry;
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) {

        RequestContext context = new DefaultRequestContext();

        lookupHeader(request, "X-TenantId", "X-Tenant").ifPresent(value -> {
            LOG.debug("Tenant found in context", value);
            context.setTenantId(value);
            TenantContextHolder.set(value);
        });
        lookupHeader(request, "X-Application", "X-ApplicationName", "X-ApplicationId", "X-App").ifPresent(context::setApplicationName);
        lookupHeader(request, "X-TraceId", "X-Trace-Id", "X-RequestId", "X-Request-Id").ifPresent(context::setTraceId);
        lookupHeader(request, "X-SpanId", "X-Span-Id", "X-CorrelationId", "X-Correlation-Id").ifPresent(context::setSpanId);
        processTracing(context);

        RequestContextHolder.set(context);

        AtomicBoolean proceed = new AtomicBoolean(true);
        //noinspection Convert2Lambda
        lookupHeader(request, HttpHeaders.AUTHORIZATION, "X-JWT-Assertion", "X-JWT-Assertions").ifPresent(new Consumer<String>() {
            @SneakyThrows
            @Override
            public void accept(String value) {
                try {
                    authManager.handle(context, value);
                } catch (Exception e) {
                    proceed.set(false);
                    int statusCode = ErrorUtil.resolveErrorCode(e);
                    if (statusCode > -1) {
                        response.setContentType("application/json");
                        response.sendError(statusCode, JsonUtil.serialize(ImmutableMap.of(
                            "message", e.getMessage()
                        )));
                    } else if (e instanceof AccessDeniedException) {
                        response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
                    } else {
                        LOG.error(e);
                        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
                    }
                }
            }
        });

        if (!proceed.get()) {
            return;
        }

        try {
            RequestContextHolder.set(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Serving request: %s %s", request.getMethod(), request.getRequestURI());
            }
            chain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
        }
    }

    private void processTracing(RequestContext context) {
        String prefix = "";
        if (context.getTenantId() != null) {
            prefix = context.getTenantId() + "_";
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
