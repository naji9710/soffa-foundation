package io.soffa.foundation.core.actions;


import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.messages.Message;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class MessagesHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(MessagesHandler.class);
    private final List<MessageHandler> handlers;

    public static MessagesHandler of(List<MessageHandler> handlers) {
        return new MessagesHandler(handlers);
    }

    @Override
    public Optional<Object> onMessage(Message message) {
        for (MessageHandler handler : handlers) {
            if (handler.accept(message.getAction())) {
                return handler.onMessage(message);
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("No handler found for action: %s", message.getAction());
        }
        return Optional.empty();
    }

    @Override
    public boolean accept(String action) {
        return !handlers.isEmpty();
    }
}
