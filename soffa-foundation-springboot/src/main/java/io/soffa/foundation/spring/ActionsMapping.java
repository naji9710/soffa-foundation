package io.soffa.foundation.spring;

import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.actions.Action;
import io.soffa.foundation.core.actions.Action0;
import io.soffa.foundation.core.annotations.BindAction;
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
    private Class<?> resolveClass(Object action) {
        Class<?> targetClass = action.getClass();
        if (AopUtils.isAopProxy(action) && action instanceof Advised) {
            Object target = ((Advised) action).getTargetSource().getTarget();
            targetClass = Objects.requireNonNull(target).getClass();
        }
        return targetClass;
    }

    private Optional<String> registerAnyBinding(Class<?> targetClass, Object action) {
        String bindingName = null;
        BindAction binding = targetClass.getAnnotation(BindAction.class);
        if (binding != null) {
            if (TextUtil.isEmpty(binding.name())) {
                throw new TechnicalException("@BindAction on a type should have the property name set.");
            }
            bindingName = binding.name();
            internal.put(binding.name(), action);
        }
        return Optional.ofNullable(bindingName);
    }

    @SneakyThrows
    private void register(Set<?> actions) {
        for (Object action : actions) {
            Class<?> targetClass = resolveClass(action);
            Optional<String> bindingName = registerAnyBinding(targetClass, action);

            for (Class<?> intf : targetClass.getInterfaces()) {
                if (Action0.class.isAssignableFrom(intf) && intf != Action0.class) {
                    internal.put(intf.getSimpleName(), action);
                    internal.put(intf.getName(), action);
                } else if (Action.class.isAssignableFrom(intf) && intf != Action.class) {
                    internal.put(intf.getSimpleName(), action);
                    internal.put(intf.getName(), action);

                    Method method = Arrays.stream(action.getClass().getMethods())
                        .filter(m -> "handle".equals(m.getName()) && 2 == m.getParameterCount() && m.getParameterTypes()[1] == RequestContext.class)
                        .findFirst().orElseThrow(() -> new TechnicalException("Invalid action definition"));

                    Class<?> inputType = method.getParameterTypes()[0];
                    inputTypes.put(intf.getSimpleName(), inputType);
                    inputTypes.put(intf.getName(), inputType);
                    bindingName.ifPresent(s -> inputTypes.put(s, inputType));
                }
            }
        }
    }


}
