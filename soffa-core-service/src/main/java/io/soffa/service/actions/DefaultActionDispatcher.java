package io.soffa.service.actions;

import io.soffa.service.core.Action;
import io.soffa.service.core.RequestContext;
import io.soffa.commons.exceptions.TechnicalException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.inject.Named;
import java.util.Set;

@Named
@AllArgsConstructor
public class DefaultActionDispatcher implements ActionDispatcher {

    private Set<Action<?,?>> registry;

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request)  {
        RequestContext context = (RequestContext)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return dispatch(actionClass, request, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request, RequestContext context)  {

        Action<I, O> impl = null;
        for (Action<?, ?> act : registry) {
            if (actionClass.isAssignableFrom(act.getClass())) {
                impl = (Action<I, O>) act;
                break;
            }
        }

        if (impl == null) {
            throw new TechnicalException("Unable to find implementation for action: {0}", actionClass.getName());
        }
        return impl.handle(request, context);
    }
}
