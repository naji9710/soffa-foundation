package io.soffa.foundation.core.actions;

import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.messages.Message;

import java.util.Optional;

public class NoopMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(NoopMessageHandler.class);

    @Override
    public boolean accept(String action) {
        return false;
    }

    @Override
    public Optional<Object> onMessage(Message message) {
        LOG.warn("NoopMessageHandler received message: %s", message.getAction());
        return Optional.empty();
    }

}
