package io.soffa.foundation.spring;

import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.actions.Action;
import io.soffa.foundation.core.actions.Action0;
import io.soffa.foundation.core.actions.ActionDispatcher;
import io.soffa.foundation.core.model.Validatable;
import io.soffa.foundation.exceptions.TechnicalException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@AllArgsConstructor
public class DefaultActionDispatcher implements ActionDispatcher {

    private final ActionsMapping mapping;

    @SuppressWarnings("unchecked")
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
                return impl.handle(request, context);
            }
        }
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
                return impl.handle(context);
            }
        }
        throw new TechnicalException("Unable to find implementation for action: %s", actionClass.getName());
    }


}
