package io.soffa.foundation.spring;

import io.soffa.foundation.actions.Action;
import io.soffa.foundation.actions.Action0;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.exceptions.TechnicalException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.*;

@Getter
public class ActionsMapping {

    private final Set<Action<?, ?>> registry;
    private final Set<Action0<?>> registry0;
    private final Map<String, Object> internal = new HashMap<>();
    private final Map<String, Class<?>> inputTypes = new HashMap<>();

    public ActionsMapping(Set<Action<?, ?>> registry, Set<Action0<?>> registry0) {
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
            internal.put(targetClass.getSimpleName(), action);
            for (Class<?> intf : targetClass.getInterfaces()) {
                if (Action0.class.isAssignableFrom(intf)) {
                    internal.put(intf.getSimpleName(), action);
                } else if (Action.class.isAssignableFrom(intf)) {
                    internal.put(intf.getSimpleName(), action);
                    Method method = Arrays.stream(action.getClass().getMethods())
                        .filter(m -> "handle".equals(m.getName()) && 2 == m.getParameterCount() && m.getParameterTypes()[1] == RequestContext.class)
                        .findFirst().orElseThrow(() -> new TechnicalException("Invalid action definition"));
                    inputTypes.put(intf.getSimpleName(), method.getParameterTypes()[0]);
                }
            }
        }
    }

}
