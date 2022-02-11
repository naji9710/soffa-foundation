package io.soffa.foundation.spring;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.core.Constants;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.RequestContextUtil;
import io.soffa.foundation.core.context.RequestContextHolder;
import io.soffa.foundation.core.exceptions.TechnicalException;
import io.soffa.foundation.core.metrics.CoreMetrics;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import io.soffa.foundation.core.model.Validatable;
import io.soffa.foundation.core.operations.Operation;
import io.soffa.foundation.core.operations.Operation0;
import io.soffa.foundation.core.operations.OperationDispatcher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

import static io.soffa.foundation.core.metrics.CoreMetrics.OPERATION_PREFIX;

@Component
@AllArgsConstructor
public class DefaultOperationDispatcher implements OperationDispatcher {

    private final OperationsMapping mapping;
    private final MetricsRegistry metricsRegistry;

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

        for (Operation<?, ?> act : mapping.getRegistry()) {
            if (operationClass.isAssignableFrom(act.getClass())) {
                Operation<I, O> impl = (Operation<I, O>) act;
                return metricsRegistry.track(OPERATION_PREFIX + operationClass.getSimpleName(), RequestContextUtil.tagify(context), () -> impl.handle(request, context));
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
        for (Operation0<?> act : mapping.getRegistry0()) {
            if (operationClass.isAssignableFrom(act.getClass())) {
                Operation0<O> impl = (Operation0<O>) act;
                return metricsRegistry.track(OPERATION_PREFIX + operationClass.getName(), ImmutableMap.of(
                    Constants.OPERATION, operationClass.getName()
                ), () -> impl.handle(context));
            }
        }
        metricsRegistry.increment(CoreMetrics.INVALID_OPERATION);
        throw new TechnicalException("Unable to find implementation for operation: %s", operationClass.getName());
    }


}
