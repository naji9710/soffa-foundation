package io.soffa.foundation.service.aop;

import io.soffa.foundation.annotations.Dispatch;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.messages.Message;
import io.soffa.foundation.messages.MessageDispatcher;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MessageDispatcherAspect {

    private final MessageDispatcher messageDispatcher;
    public static final Logger LOG = Logger.get(MessageDispatcherAspect.class);

    public MessageDispatcherAspect(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    @SneakyThrows
    @Around("@annotation(message)")
    public Object checkAuthenticated(ProceedingJoinPoint pjp, Dispatch message) {
        Object result = pjp.proceed(pjp.getArgs());
        String eventId = message.value();
        messageDispatcher.broadcast(new Message(eventId, result));
        LOG.info("Message dispatched: %s", eventId);
        return result;
    }


}
