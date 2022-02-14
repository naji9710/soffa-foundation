package io.soffa.foundation.service.aop;

import io.soffa.foundation.annotations.Publish;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.messages.Message;
import io.soffa.foundation.messages.PubSubClient;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PublishMessageAspect {

    private final PubSubClient pubSub;

    public PublishMessageAspect(@Autowired(required = false) PubSubClient pubSub) {
        this.pubSub = pubSub;
    }

    public static final Logger LOG = Logger.get(PublishMessageAspect.class);

    @SneakyThrows
    @Around("@annotation(message)")
    public Object publishMessage(ProceedingJoinPoint pjp, Publish message) {
        Object result = pjp.proceed(pjp.getArgs());
        if (pubSub == null) {
            LOG.warn("Unable to honor @Publish annotation because no PubSubClient is registered");
        } else {
            try {
                String eventId = message.value();
                pubSub.broadcast(new Message(eventId, result));
                LOG.info("Message dispatched: %s", eventId);
            } catch (Exception e) {
                LOG.error(e, "Failed to publish message %s -- %s", message.value(), e.getMessage());
                //TODO: we should requeue the message and retry later
            }
        }
        return result;
    }


}
