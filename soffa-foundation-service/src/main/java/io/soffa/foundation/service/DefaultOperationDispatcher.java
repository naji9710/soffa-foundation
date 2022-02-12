package io.soffa.foundation.service;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.Constants;
import io.soffa.foundation.api.Operation;
import io.soffa.foundation.api.Operation0;
import io.soffa.foundation.application.OperationDispatcher;
import io.soffa.foundation.commons.ErrorUtil;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.RequestContextUtil;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.exceptions.UnauthorizedException;
import io.soffa.foundation.metrics.CoreMetrics;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.model.Validatable;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

import static io.soffa.foundation.metrics.CoreMetrics.OPERATION_PREFIX;

@Component
@AllArgsConstructor
public class DefaultOperationDispatcher implements OperationDispatcher {

    private final OperationsMapping mapping;
    private final MetricsRegistry metricsRegistry;
    private final PlatformAuthManager authManager;

    @Override
    public <I, O> O dispatch(Class<? extends Operation<I, O>> operationClass, I request) {
        RequestContext context = RequestContextHolder.require();
        return dispatch(operationClass, request, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O dispatch(Class<? extends Operation<I, O>> operationClass, I request, RequestContext context) {
        if (request instanceof Validatable) {
            ((Validatable) request).validate();
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null && context.hasAuthorization()) {
            authManager.process(context);
        }

        for (Operation<?, ?> act : mapping.getRegistry()) {
            if (operationClass.isAssignableFrom(act.getClass())) {
                Operation<I, O> impl = (Operation<I, O>) act;
                return metricsRegistry.track(OPERATION_PREFIX + operationClass.getSimpleName(), RequestContextUtil.tagify(context), () -> {
                    try {
                        return impl.handle(request, context);
                    } catch (AuthenticationCredentialsNotFoundException e) {
                        throw new UnauthorizedException(e.getMessage(), ErrorUtil.getStacktrace(e));
                    }
                });
            }
        }
        metricsRegistry.increment(CoreMetrics.INVALID_OPERATION);
        throw new TechnicalException("Unable to find implementation for operation: %s", operationClass.getName());
    }

    @Override
    public <O> O dispatch(Class<? extends Operation0<O>> operationClass) {
        RequestContext context = RequestContextHolder.require();
        return dispatch(operationClass, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public <O> O dispatch(Class<? extends Operation0<O>> operationClass, RequestContext context) {

        if (SecurityContextHolder.getContext().getAuthentication() == null && context.hasAuthorization()) {
            authManager.process(context);
        }

        for (Operation0<?> act : mapping.getRegistry0()) {
            if (operationClass.isAssignableFrom(act.getClass())) {
                Operation0<O> impl = (Operation0<O>) act;
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


}
