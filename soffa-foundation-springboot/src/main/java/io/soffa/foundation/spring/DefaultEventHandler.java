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

@Component
@AllArgsConstructor
public class DefaultEventHandler implements EventHandler {

    private static final Logger LOG = Logger.get(DefaultEventHandler.class);
    private final ActionsMapping mapping;

    @SneakyThrows
    @Override
    public void handle(Event event) {
        Object action = mapping.getInternal().get(event.getAction());
        if (action == null) {
            LOG.error("No action handle found to event %s, dont't forget to use the action simple class name");
            return;
        }
        if (action instanceof Action) {
            Class<?> inputType = mapping.getInputTypes().get(event.getAction());
            Object payload = event.getPayloadAs(inputType).orElse(null);
            MethodUtils.invokeMethod(action, "handle", new Object[]{payload, event.getContext()});
        } else {
            ((Action0<?>) action).handle(event.getContext());
        }
    }



}
