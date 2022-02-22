package io.soffa.foundation.service.core;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.Operation;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.context.RequestContextHolder;
import io.soffa.foundation.core.context.RequestContextUtil;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.core.messages.MessageFactory;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import io.soffa.foundation.core.operation.OperationsMapping;
import io.soffa.foundation.core.pubsub.MessageHandler;
import io.soffa.foundation.core.security.PlatformAuthManager;
import io.soffa.foundation.errors.TechnicalException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Component
@AllArgsConstructor
public class DefaultMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(DefaultMessageHandler.class);
    private final OperationsMapping mapping;
    private final MetricsRegistry metricsRegistry;
    private final PlatformAuthManager authManager;

    @Override
    public Optional<Object> handle(@NonNull Message message) {
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

        final AtomicReference<Object> payload = new AtomicReference<>();

        if (message.getPayload() != null) {
            if (TextUtil.isNotEmpty(message.getPayloadType())) {
                try {
                    LOG.debug("Deserializing message content into %s", message.getPayloadType());
                    payload.set(MessageFactory.getPayload(message, Class.forName(message.getPayloadType())));
                } catch (ClassNotFoundException e) {
                    LOG.error("Unable to deserialize message into %s", message.getPayloadType());
                }
            }
            if (payload.get() == null) {
                LOG.debug("Deserializing message content into %s", inputType.getName());
                payload.set(MessageFactory.getPayload(message, inputType));
            }
        }

        //noinspection Convert2Lambda
        return metricsRegistry.track(
            "app_operation_" + message.getOperation(),
            RequestContextUtil.tagify(context),
            new Supplier<Optional<Object>>() {
                @SneakyThrows
                @Override
                public Optional<Object> get() {
                    if (payload.get() == null) {
                        LOG.debug("Invoking operation %s with empty payload", operation.getClass().getSimpleName());
                    } else {
                        LOG.debug("Invoking operation %s with payload of type %s", operation.getClass().getSimpleName(), payload.get().getClass().getSimpleName());
                    }
                    //noinspection unchecked
                    @SuppressWarnings("unchecked")
                    Object result = ((Operation<Object, Object>) operation).handle(payload.get(), context);
                    if (result==null) {
                        return Optional.empty();
                    }
                    return Optional.of(result);
                }
            });
    }


}
