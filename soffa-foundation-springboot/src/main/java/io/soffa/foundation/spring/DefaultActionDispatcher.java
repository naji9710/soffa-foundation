package io.soffa.foundation.spring;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.actions.Action;
import io.soffa.foundation.core.actions.Action0;
import io.soffa.foundation.core.actions.ActionDispatcher;
import io.soffa.foundation.core.metrics.CoreMetrics;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import io.soffa.foundation.core.model.Validatable;
import io.soffa.foundation.exceptions.TechnicalException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

import static io.soffa.foundation.core.metrics.CoreMetrics.ACTION_HANDLE;

@Component
@AllArgsConstructor
public class DefaultActionDispatcher implements ActionDispatcher {

    private final ActionsMapping mapping;
    private final MetricsRegistry metricsRegistry;

    @Override
    public <I, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request) {
        RequestContext context = RequestContextHolder.require();
        return dispatch(actionClass, request, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request, RequestContext context) {
        if (request instanceof Validatable) {
            ((Validatable) request).validate();
        }
        for (Action<?, ?> act : mapping.getRegistry()) {
            if (actionClass.isAssignableFrom(act.getClass())) {
                Action<I, O> impl = (Action<I, O>) act;
                return metricsRegistry.track(ACTION_HANDLE, ImmutableMap.of(
                    "action", actionClass.getName()
                ), () -> impl.handle(request, context));
            }
        }
        metricsRegistry.increment(CoreMetrics.INVALID_ACTION);
        throw new TechnicalException("Unable to find implementation for action: %s", actionClass.getName());
    }

    @Override
    public <O> O dispatch(Class<? extends Action0<O>> actionClass) {
        RequestContext context = RequestContextHolder.require();
        return dispatch(actionClass, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public <O> O dispatch(Class<? extends Action0<O>> actionClass, RequestContext context) {
        for (Action0<?> act : mapping.getRegistry0()) {
            if (actionClass.isAssignableFrom(act.getClass())) {
                Action0<O> impl = (Action0<O>) act;
                return metricsRegistry.track(ACTION_HANDLE, ImmutableMap.of(
                    "action", actionClass.getName()
                ), () -> impl.handle(context));
            }
        }
        metricsRegistry.increment(CoreMetrics.INVALID_ACTION);
        throw new TechnicalException("Unable to find implementation for action: %s", actionClass.getName());
    }


}
