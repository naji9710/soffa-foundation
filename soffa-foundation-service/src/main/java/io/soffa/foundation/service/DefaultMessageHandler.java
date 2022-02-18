package io.soffa.foundation.service;

import io.soffa.foundation.api.Operation;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.RequestContextUtil;
import io.soffa.foundation.errors.TechnicalException;
import io.soffa.foundation.messages.MessageFactory;
import io.soffa.foundation.metrics.MetricsRegistry;
import io.soffa.foundation.model.Message;
import io.soffa.foundation.pubsub.MessageHandler;
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
    private final PlatformAuthManager authManager;

    @Override
    public Optional<Object> handle(Message message) {
        final RequestContext context = message.getContext();
        RequestContextHolder.set(context);
        Object operation = mapping.getInternal().get(message.getOperation());
        if (operation == null) {
            LOG.debug("Message %s skipped, no local handler registered", message.getOperation());
            return Optional.empty();
        }

        if (authManager != null && context.hasAuthorization()) {
            authManager.handle(context);
        }

        LOG.debug("New message received with operation %s#%s", message.getOperation(), message.getId());

        if (!(operation instanceof Operation)) {
            throw new TechnicalException("Unsupported operation type: " + operation.getClass().getName());
        }

        Class<?> inputType = mapping.getInputTypes().get(message.getOperation());
        if (inputType == null) {
            throw new TechnicalException("Unable to find input type for operation " + message.getOperation());
        }

        Object payload = MessageFactory.getPayload(message, inputType);
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
                    Object result = ((Operation<Object, ?>) operation).handle(payload, context);
                    if (result==null) {
                        return Optional.empty();
                    }
                    return Optional.of(result);
                }
            });
    }


}
