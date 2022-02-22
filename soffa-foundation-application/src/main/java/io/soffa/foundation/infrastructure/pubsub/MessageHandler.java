package io.soffa.foundation.infrastructure.pubsub;


import io.soffa.foundation.application.messages.Message;

import java.util.Optional;

public interface MessageHandler {

    Optional<Object> handle(Message message);

}
