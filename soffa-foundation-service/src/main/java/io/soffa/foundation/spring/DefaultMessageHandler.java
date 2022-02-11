package io.soffa.foundation.spring;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.RequestContextUtil;
import io.soffa.foundation.core.exceptions.TechnicalException;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import io.soffa.foundation.core.operations.MessageHandler;
import io.soffa.foundation.core.operations.Operation;
import io.soffa.foundation.core.operations.Operation0;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.reflect.MethodUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

@Component
@AllArgsConstructor
public class DefaultMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(DefaultMessageHandler.class);
    private final OperationsMapping mapping;
    private final MetricsRegistry metricsRegistry;

    @Override
    public boolean accept(String operationId) {
        return mapping.getInternal().containsKey(operationId);
    }

    @Override
    public Optional<Object> onMessage(Message message) {
        final RequestContext context = Optional.ofNullable(message.getContext()).orElse(new RequestContext());

        Object operation = mapping.getInternal().get(message.getOperation());

        if (operation == null) {
            LOG.error("No handler found for event %s, dont't forget to use the operation simple class name", message.getOperation());
            return Optional.empty();
        }
        if (operation instanceof Operation) {
            Class<?> inputType = mapping.getInputTypes().get(message.getOperation());
            if (inputType == null) {
                throw new TechnicalException("Unable to find input type for operation " + message.getOperation());
            }
            Object payload = message.getPayloadAs(inputType).orElse(null);
            if (payload == null) {
                throw new TechnicalException("Operation " + operation.getClass().getName() + " is expecting a null input");
            }
            //noinspection Convert2Lambda
            return metricsRegistry.track(
                "app_operation_" + message.getOperation(),
                RequestContextUtil.tagify(context), new Supplier<Optional<Object>>() {
                    @SneakyThrows
                    @Override
                    public Optional<Object> get() {
                        return Optional.ofNullable(MethodUtils.invokeMethod(operation, "handle", new Object[]{payload, context}));
                    }
                });
        } else if (operation instanceof Operation0) {
            return metricsRegistry.track(
                "app_operation_" + message.getOperation(),
                RequestContextUtil.tagify(context), () -> {
                    //EL
                    return Optional.ofNullable(((Operation0<?>) operation).handle(context));
                });
        } else {
            throw new TechnicalException("Unsupported operation type: " + operation.getClass().getName());
        }
    }


}
