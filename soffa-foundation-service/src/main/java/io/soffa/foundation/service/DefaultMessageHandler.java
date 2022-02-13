package io.soffa.foundation.service;

import io.soffa.foundation.api.Operation;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextUtil;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.messages.Message;
import io.soffa.foundation.messages.MessageHandler;
import io.soffa.foundation.metrics.MetricsRegistry;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
    public boolean accept(String operation) {
        return mapping.getInternal().containsKey(operation);
    }

    @Override
    public Optional<Object> handle(Message message) {
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
            //noinspection Convert2Lambda
            return metricsRegistry.track(
                "app_operation_" + message.getOperation(),
                RequestContextUtil.tagify(context), new Supplier<Optional<Object>>() {
                    @SneakyThrows
                    @Override
                    public Optional<Object> get() {
                        //noinspection unchecked
                        return Optional.ofNullable(((Operation<Object, ?>) operation).handle(payload, context));
                    }
                });
        } else {
            throw new TechnicalException("Unsupported operation type: " + operation.getClass().getName());
        }
    }


}
