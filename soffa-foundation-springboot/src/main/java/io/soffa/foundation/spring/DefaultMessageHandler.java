package io.soffa.foundation.spring;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.actions.Action;
import io.soffa.foundation.core.actions.Action0;
import io.soffa.foundation.core.actions.MessageHandler;
import io.soffa.foundation.core.messages.Message;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.reflect.MethodUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class DefaultMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(DefaultMessageHandler.class);
    private final ActionsMapping mapping;

    @Override
    public boolean accept(String action) {
        return mapping.getInternal().containsKey(action);
    }

    @SneakyThrows
    @Override
    public Optional<Object> onMessage(Message message) {

        RequestContext context = message.getContext();
        if (context == null) {
            context = new RequestContext();
        }

        Object action = mapping.getInternal().get(message.getAction());
        if (action == null) {
            LOG.error("No handler found for event %s, dont't forget to use the action simple class name", message.getAction());
            return Optional.empty();
        }
        if (action instanceof Action) {
            Class<?> inputType = mapping.getInputTypes().get(message.getAction());
            Object payload = message.getPayloadAs(inputType).orElse(null);
            return Optional.ofNullable(MethodUtils.invokeMethod(action, "handle", new Object[]{payload, context}));
        } else {
            return Optional.ofNullable(((Action0<?>) action).handle(context));
        }
    }


}
