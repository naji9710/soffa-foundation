package io.soffa.foundation.service.aop;

import io.soffa.foundation.annotations.Publish;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.messages.Message;
import io.soffa.foundation.messages.PubSubClient;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnBean(PubSubClient.class)
@AllArgsConstructor
public class PublishMessageAspect {

    private final PubSubClient pubSub;
    public static final Logger LOG = Logger.get(PublishMessageAspect.class);

    @SneakyThrows
    @Around("@annotation(message)")
    public Object checkAuthenticated(ProceedingJoinPoint pjp, Publish message) {
        Object result = pjp.proceed(pjp.getArgs());
        String eventId = message.value();
        pubSub.broadcast(new Message(eventId, result));
        LOG.info("Message dispatched: %s", eventId);
        return result;
    }


}
