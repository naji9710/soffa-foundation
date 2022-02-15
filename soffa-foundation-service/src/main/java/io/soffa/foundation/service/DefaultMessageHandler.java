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
            LOG.debug("Message %s skipped, no local handler registered", message.getOperation());
            return Optional.empty();
        }

        LOG.debug("New message received with operation %s#%s", message.getOperation(), message.getId());

        if (!(operation instanceof Operation)) {
            throw new TechnicalException("Unsupported operation type: " + operation.getClass().getName());
        }
        /*
        Class<?> inputType = mapping.getInputTypes().get(message.getOperation());
        if (inputType == null) {
            throw new TechnicalException("Unable to find input type for operation " + message.getOperation());
        }
         */

        Object payload = message.deserialize();
        //noinspection Convert2Lambda
        return metricsRegistry.track(
            "app_operation_" + message.getOperation(),
            RequestContextUtil.tagify(context),
            new Supplier<Optional<Object>>() {
                @SneakyThrows
                @Override
                public Optional<Object> get() {
                    if (payload == null) {
                        LOG.debug("Invoking operation %s with empty payload", operation.getClass().getSimpleName());
                    } else {
                        LOG.debug("Invoking operation %s with empty payload of type %s", operation.getClass().getSimpleName(), payload.getClass().getSimpleName());
                    }
                    //noinspection unchecked
                    return Optional.ofNullable(((Operation<Object, ?>) operation).handle(payload, context));
                }
            });
    }


}
