package io.soffa.foundation.spring;

import io.soffa.foundation.actions.Action;
import io.soffa.foundation.actions.Action0;
import io.soffa.foundation.actions.ActionDispatcher;
import io.soffa.foundation.actions.EventHandler;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.model.Validatable;
import io.soffa.foundation.events.Event;
import io.soffa.foundation.exceptions.TechnicalException;
import lombok.SneakyThrows;
import org.apache.commons.lang.reflect.MethodUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import javax.transaction.Transactional;
import java.lang.reflect.Method;
import java.util.*;

public class DefaultActionDispatcher implements ActionDispatcher, EventHandler {

    private static final Logger LOG = Logger.get(DefaultActionDispatcher.class);
    private final Set<Action<?, ?>> registry;
    private final Set<Action0<?>> registry0;
    private final Map<String, Object> actionsMapping = new HashMap<>();
    private final Map<String, Class<?>> inputTypes = new HashMap<>();


    public DefaultActionDispatcher(Set<Action<?, ?>> registry, Set<Action0<?>> registry0) {
        this.registry = registry;
        this.registry0 = registry0;
        register(registry);
        register(registry0);
    }

    @SneakyThrows
    private void register(Set<?> actions) {
        for (Object action : actions) {
            Class<?> targetClass = action.getClass();
            if(AopUtils.isAopProxy(action) && action instanceof Advised) {
                Object target = ((Advised)action).getTargetSource().getTarget();
                targetClass = Objects.requireNonNull(target).getClass();
            }
            actionsMapping.put(targetClass.getSimpleName(), action);
            for (Class<?> intf : targetClass.getInterfaces()) {
                if (Action0.class.isAssignableFrom(intf)) {
                    actionsMapping.put(intf.getSimpleName(), action);
                } else if (Action.class.isAssignableFrom(intf)) {
                    actionsMapping.put(intf.getSimpleName(), action);
                    Method method = Arrays.stream(action.getClass().getMethods())
                        .filter(m -> "handle".equals(m.getName()) && 2 == m.getParameterCount() && m.getParameterTypes()[1] == RequestContext.class)
                        .findFirst().orElseThrow(() -> new TechnicalException("Invalid action definition"));
                    inputTypes.put(intf.getSimpleName(), method.getParameterTypes()[0]);
                }
            }
        }
    }

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
        for (Action<?, ?> act : registry) {
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
        for (Action0<?> act : registry0) {
            if (actionClass.isAssignableFrom(act.getClass())) {
                Action0<O> impl = (Action0<O>) act;
                return impl.handle(context);
            }
        }
        throw new TechnicalException("Unable to find implementation for action: %s", actionClass.getName());
    }

    @SneakyThrows
    @Override
    public void handle(Event event) {
        Object action = actionsMapping.get(event.getAction());
        if (action == null) {
            LOG.error("No action handle found to event %s, dont't forget to use the action simple class name");
            return;
        }
        if (action instanceof Action) {
            Class<?> inputType = inputTypes.get(event.getAction());
            Object payload = event.getPayloadAs(inputType).orElse(null);
            MethodUtils.invokeMethod(action, "handle", new Object[]{payload, event.getContext()});
        } else {
            ((Action0<?>) action).handle(event.getContext());
        }
    }



}
