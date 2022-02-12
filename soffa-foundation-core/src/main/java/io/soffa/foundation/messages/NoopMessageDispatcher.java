package io.soffa.foundation.messages;

import io.soffa.foundation.commons.Logger;

public class NoopMessageDispatcher implements MessageDispatcher {

    private static final Logger LOG = Logger.get(NoopMessageDispatcher.class);

    @Override
    public void broadcast(Message event) {
        if (LOG.isInfoEnabled()) {
            LOG.info("[NoopMessageDispatcher] dispatch event: " + event.getOperation());
        }
    }

}
