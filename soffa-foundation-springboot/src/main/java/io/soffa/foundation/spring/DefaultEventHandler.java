package io.soffa.foundation.spring;

import io.soffa.foundation.actions.Action;
import io.soffa.foundation.actions.Action0;
import io.soffa.foundation.actions.EventHandler;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.events.Event;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.reflect.MethodUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class DefaultEventHandler implements EventHandler {

    private static final Logger LOG = Logger.get(DefaultEventHandler.class);
    private final ActionsMapping mapping;

    @SneakyThrows
    @Override
    public Optional<Object> handle(Event event) {
        Object action = mapping.getInternal().get(event.getAction());
        if (action == null) {
            LOG.error("No handler found for event %s, dont't forget to use the action simple class name", event.getAction());
            return Optional.empty();
        }
        if (action instanceof Action) {
            Class<?> inputType = mapping.getInputTypes().get(event.getAction());
            Object payload = event.getPayloadAs(inputType).orElse(null);
            return Optional.ofNullable(MethodUtils.invokeMethod(action, "handle", new Object[]{payload, event.getContext()}));
        } else {
            return Optional.ofNullable(((Action0<?>) action).handle(event.getContext()));
        }
    }


}
