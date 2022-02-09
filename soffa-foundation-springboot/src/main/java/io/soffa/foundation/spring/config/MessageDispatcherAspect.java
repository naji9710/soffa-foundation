package io.soffa.foundation.spring.config;

import io.soffa.foundation.annotations.DispatchMessage;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.core.messages.MessageDispatcher;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
//@ConditionalOnBean(MessageDispatcher.class)
public class MessageDispatcherAspect {

    private final MessageDispatcher messageDispatcher;
    public static final Logger LOG = Logger.get(MessageDispatcherAspect.class);

    public MessageDispatcherAspect(@Autowired(required = false) MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    @SneakyThrows
    @Around("@annotation(message)")
    public Object checkAuthenticated(ProceedingJoinPoint pjp, DispatchMessage message) {
        Object result = pjp.proceed(pjp.getArgs());
        String eventId = message.value();
        messageDispatcher.broadcast(new Message(eventId, result));
        LOG.info("Message dispatched: %s", eventId);
        return result;
    }


}
