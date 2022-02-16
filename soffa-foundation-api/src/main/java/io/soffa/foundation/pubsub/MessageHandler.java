package io.soffa.foundation.pubsub;


import io.soffa.foundation.model.Message;

import java.util.Optional;

public interface MessageHandler {

    Optional<Object> handle(Message message);

}
