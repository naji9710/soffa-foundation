package io.soffa.foundation.service.aop;

import io.soffa.foundation.annotations.Publish;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.messages.MessageFactory;
import io.soffa.foundation.model.Message;
import io.soffa.foundation.pubsub.PubSubClient;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PublishMessageAspect {

    private static final Logger LOG = Logger.get(PublishMessageAspect.class);
    private final PubSubClient pubSub;

    public PublishMessageAspect(@Autowired(required = false) PubSubClient pubSub) {
        this.pubSub = pubSub;
    }

    @SneakyThrows
    @Around("@annotation(publish)")
    public Object publishMessage(ProceedingJoinPoint pjp, Publish publish) {
        Object result = pjp.proceed(pjp.getArgs());
        if (pubSub == null) {
            LOG.warn("Unable to honor @Publish annotation because no PubSubClient is registered");
        } else {
            try {
                String event = publish.event();
                String subject = publish.target();
                Message msg = MessageFactory.create(event, result);
                if ("*".equalsIgnoreCase(subject)) {
                    pubSub.broadcast(subject, msg);
                }else {
                    pubSub.publish(subject, msg);
                }
                LOG.info("Message dispatched: %s", event);
            } catch (Exception e) {
                LOG.error(e, "Failed to publish message %s -- %s", publish.event(), e.getMessage());
                //TODO: we should requeue the message and retry later
            }
        }
        return result;
    }


}
