package io.soffa.foundation.service;

import io.soffa.foundation.api.Operation;
import io.soffa.foundation.application.OperationHandler;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.RequestContextUtil;
import io.soffa.foundation.errors.ErrorUtil;
import io.soffa.foundation.errors.TechnicalException;
import io.soffa.foundation.errors.UnauthorizedException;
import io.soffa.foundation.metrics.CoreMetrics;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.model.Validatable;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static io.soffa.foundation.metrics.CoreMetrics.OPERATION_PREFIX;

@Component
@AllArgsConstructor
public class DefaultOperationHandler implements OperationHandler {

    private final OperationsMapping mapping;
    private final MetricsRegistry metricsRegistry;
    private final PlatformAuthManager authManager;

    @Override
    public <I, O> O handle(Class<? extends Operation<I, O>> operationClass, I request) {
        RequestContext context = RequestContextHolder.require();
        return handle(operationClass, request, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O handle(Class<? extends Operation<I, O>> operationClass, I input, RequestContext context) {
        if (input instanceof Validatable) {
            ((Validatable) input).validate();
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null && context.hasAuthorization()) {
            authManager.handle(context);
        }

        for (Operation<?, ?> act : mapping.getRegistry()) {
            if (operationClass.isAssignableFrom(act.getClass())) {
                Operation<I, O> impl = (Operation<I, O>) act;
                return metricsRegistry.track(OPERATION_PREFIX + operationClass.getSimpleName(), RequestContextUtil.tagify(context), () -> {
                    try {
                        return impl.handle(input, context);
                    } catch (AuthenticationCredentialsNotFoundException e) {
                        throw new UnauthorizedException(e.getMessage(), ErrorUtil.getStacktrace(e));
                    }
                });
            }
        }
        metricsRegistry.increment(CoreMetrics.INVALID_OPERATION);
        throw new TechnicalException("Unable to find implementation for operation: %s", operationClass.getName());
    }

   /*
    @Override
    public <O> O handle(Class<? extends NoInputOperation<O>> operationClass) {
        RequestContext context = RequestContextHolder.require();
        return handle(operationClass, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public <O> O handle(Class<? extends NoInputOperation<O>> operationClass, RequestContext context) {

        if (SecurityContextHolder.getContext().getAuthentication() == null && context.hasAuthorization()) {
            authManager.process(context);
        }

        for (NoInputOperation<?> act : mapping.getRegistry0()) {
            if (operationClass.isAssignableFrom(act.getClass())) {
                NoInputOperation<O> impl = (NoInputOperation<O>) act;
                return metricsRegistry.track(OPERATION_PREFIX + operationClass.getName(), ImmutableMap.of(
                    Constants.OPERATION, operationClass.getName()
                ), () -> {
                    try {
                        return impl.handle(context);
                    } catch (AuthenticationCredentialsNotFoundException e) {
                        throw new UnauthorizedException(e.getMessage(), ErrorUtil.getStacktrace(e));
                    }
                });
            }
        }
        metricsRegistry.increment(CoreMetrics.INVALID_OPERATION);
        throw new TechnicalException("Unable to find implementation for operation: %s", operationClass.getName());
    }
    */

}
