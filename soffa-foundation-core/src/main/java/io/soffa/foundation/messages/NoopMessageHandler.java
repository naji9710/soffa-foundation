package io.soffa.foundation.messages;

import io.soffa.foundation.commons.Logger;

public class NoopMessageHandler implements MessageDispatcher {

    private static final Logger LOG = Logger.get(NoopMessageHandler.class);

    @Override
    public void broadcast(Message event) {
        if (LOG.isInfoEnabled()) {
            LOG.info("[NoopMessageHandler] dispatch event: " + event.getOperation());
        }
    }

}
