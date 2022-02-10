package io.soffa.foundation.spring.aop;

import io.soffa.foundation.annotations.TrackMetric;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.metrics.MetricsRegistry;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Aspect
@Component
//@ConditionalOnBean(MessageDispatcher.class)
@AllArgsConstructor
public class TrackMetricAspect {

    private final MetricsRegistry metricsRegistry;

    @SneakyThrows
    @Around("@annotation(arg)")
    public Object handle(ProceedingJoinPoint pjp, TrackMetric arg) {
        RequestContext context = RequestContextHolder.get().orElse(new RequestContext());
        //noinspection Convert2Lambda
        return metricsRegistry.track(arg.value(), context.tags(), new Supplier<Object>() {
            @Override
            @SneakyThrows
            public Object get() {
                return pjp.proceed(pjp.getArgs());
            }
        });
    }


}
