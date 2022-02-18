package io.soffa.foundation.service;

import io.soffa.foundation.annotations.Handle;
import io.soffa.foundation.api.Operation;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.errors.TechnicalException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.*;

@Getter
public class OperationsMapping {

    private final Set<Operation<?, ?>> registry;
    // private final Set<NoInputOperation<?>> registry0;
    private final Map<String, Object> internal = new HashMap<>();
    private final Map<String, Class<?>> inputTypes = new HashMap<>();

    public OperationsMapping(Set<Operation<?, ?>> registry) {
        this.registry = registry;
        register(registry);
    }

    @SneakyThrows
    private Class<?> resolveClass(Object op) {
        Class<?> targetClass = op.getClass();
        if (AopUtils.isAopProxy(op) && op instanceof Advised) {
            Object target = ((Advised) op).getTargetSource().getTarget();
            targetClass = Objects.requireNonNull(target).getClass();
        }
        return targetClass;
    }

    private Optional<String> registerAnyBinding(Class<?> targetClass, Object operation) {
        String bindingName = null;
        Handle binding = targetClass.getAnnotation(Handle.class);
        if (binding != null) {
            if (TextUtil.isEmpty(binding.value())) {
                throw new TechnicalException("@BindOperation on a type should have the property name set.");
            }
            bindingName = binding.value();
            internal.put(binding.value(), operation);
        }
        return Optional.ofNullable(bindingName);
    }

    @SneakyThrows
    private void register(Set<?> operations) {
        for (Object operation : operations) {
            Class<?> targetClass = resolveClass(operation);
            String bindingName = registerAnyBinding(targetClass, operation).orElse(null);

            for (Class<?> intf : targetClass.getInterfaces()) {
                if (Operation.class.isAssignableFrom(intf)) {
                    Method method = Arrays.stream(operation.getClass().getMethods())
                        .filter(m -> "handle".equals(m.getName()) && 2 == m.getParameterCount() && m.getParameterTypes()[1] == RequestContext.class)
                        .findFirst().orElseThrow(() -> new TechnicalException("Invalid operation definition"));

                    if (intf != Operation.class) {
                        register(intf, operation, method, bindingName);
                    }else {
                        register(targetClass, operation, method, bindingName);
                    }
                    break;
                }
            }
        }
    }

    private void register(Class<?> target, Object operation, Method method, String bindingName) {
        internal.put(target.getSimpleName(), operation);
        internal.put(target.getName(), operation);


        Class<?> inputType = method.getParameterTypes()[0];
        inputTypes.put(target.getSimpleName(), inputType);
        inputTypes.put(target.getName(), inputType);

        if (bindingName!=null) {
            inputTypes.put(bindingName, inputType);
        }

    }



}
