package io.soffa.foundation.core.messages;


public interface AmqpClient extends MessageDispatcher {

    void send(String client, String exchange, String routingKey, Message event);

    void send(String channel, Message event);

    Object request(Message event);

    <T> T request(Message event, Class<T> kind);

    void publishSelf(Message event);

}
