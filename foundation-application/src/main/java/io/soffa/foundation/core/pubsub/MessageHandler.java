package io.soffa.foundation.core.pubsub;


import io.soffa.foundation.core.messages.Message;

import java.util.Optional;

public interface MessageHandler {

    Optional<Object> handle(Message message);

}
