package io.soffa.foundation.spring;

import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.annotations.Handle;
import io.soffa.foundation.core.exceptions.TechnicalException;
import io.soffa.foundation.core.operations.Operation0;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.*;

@Getter
public class OperationsMapping {

    private final Set<io.soffa.foundation.core.operations.Operation<?, ?>> registry;
    private final Set<Operation0<?>> registry0;
    private final Map<String, Object> internal = new HashMap<>();
    private final Map<String, Class<?>> inputTypes = new HashMap<>();

    public OperationsMapping(Set<io.soffa.foundation.core.operations.Operation<?, ?>> registry, Set<Operation0<?>> registry0) {
        this.registry = registry;
        this.registry0 = registry0;
        register(registry);
        register(registry0);
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
                throw new TechnicalException("@Operation on a type should have the property name set.");
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
            Optional<String> bindingName = registerAnyBinding(targetClass, operation);

            for (Class<?> intf : targetClass.getInterfaces()) {
                if (Operation0.class.isAssignableFrom(intf) && intf != Operation0.class) {
                    internal.put(intf.getSimpleName(), operation);
                    internal.put(intf.getName(), operation);
                } else if (io.soffa.foundation.core.operations.Operation.class.isAssignableFrom(intf) && intf != io.soffa.foundation.core.operations.Operation.class) {
                    internal.put(intf.getSimpleName(), operation);
                    internal.put(intf.getName(), operation);

                    Method method = Arrays.stream(operation.getClass().getMethods())
                        .filter(m -> "handle".equals(m.getName()) && 2 == m.getParameterCount() && m.getParameterTypes()[1] == RequestContext.class)
                        .findFirst().orElseThrow(() -> new TechnicalException("Invalid operation definition"));

                    Class<?> inputType = method.getParameterTypes()[0];
                    inputTypes.put(intf.getSimpleName(), inputType);
                    inputTypes.put(intf.getName(), inputType);
                    bindingName.ifPresent(s -> inputTypes.put(s, inputType));
                }
            }
        }
    }


}
