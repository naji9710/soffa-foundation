package io.soffa.foundation.spring.config;

import io.soffa.foundation.annotations.SysLog;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.data.SysLogRepository;
import io.soffa.foundation.exceptions.ManagedException;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.models.commons.Pair;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.soffa.foundation.core.actions.ActionDispatcher.SLOW_ACTION_THRESHOLD;

@Aspect
@Component
@ConditionalOnBean(SysLogRepository.class)
@AllArgsConstructor
public class TraceActionAspect {

    public static final Logger LOG = Logger.get(TraceActionAspect.class);
    private static final ScheduledExecutorService SC = Executors.newScheduledThreadPool(16);
    private final SysLogRepository sysLogs;

    @SneakyThrows
    @Around("@annotation(trace)")
    public Object traceAction(ProceedingJoinPoint pjp, SysLog trace) {
        Instant start = Instant.now();

        Pair<RequestContext, Object> args = getArgs(pjp);

        Throwable error = null;
        String actionName = pjp.getSignature().getName();
        if (TextUtil.isNotEmpty(trace.value())) {
            actionName = trace.value();
        }
        Object result = null;
        try {
            result = pjp.proceed(pjp.getArgs());
        } catch (Exception e) {
            error = e;
        } finally {
            Instant finish = Instant.now();
            final Duration timeElapsed = Duration.between(start, finish);
            if (timeElapsed.getSeconds() >= SLOW_ACTION_THRESHOLD) {
                LOG.warn("action %s tooks more than %ds", actionName, timeElapsed.getSeconds());
            }
            String tenantId = null;
            if (args.getFirst() != null) {
                tenantId = args.getFirst().getTenant();
            }
            if (tenantId == null) {
                tenantId = TenantHolder.get().orElse(null);
            }
            if (trace.async()) {
                doLogActionAsync(tenantId, args.getFirst(), actionName, args.getSecond(), timeElapsed, error);
            } else {
                doLogAction(tenantId, args.getFirst(), actionName, args.getSecond(), timeElapsed, error);
            }
        }

        if (error != null) {
            if (ManagedException.class.isAssignableFrom(error.getClass())) {
                throw error;
            }
            throw new TechnicalException(error.getMessage(), error);
        }

        return result;
    }

    private Pair<RequestContext, Object> getArgs(ProceedingJoinPoint pjp) {
        RequestContext context = null;
        Object data = null;

        if (pjp.getArgs() != null) {
            for (Object o : pjp.getArgs()) {
                if (o instanceof RequestContext && context == null) {
                    context = (RequestContext) o;
                } else if (data == null) {
                    data = o;
                }
                if (data != null && context != null) {
                    break;
                }
            }
        }
        if (context == null) {
            context = RequestContextHolder.get().orElse(null);
        }
        return new Pair<>(context, data);
    }

    private void doLogActionAsync(String tenantId, RequestContext context, String action, Object data, Duration timeElapsed, Throwable error) {
        SC.schedule(() -> {
            doLogAction(tenantId, context, action, data, timeElapsed, error);
        }, 100, TimeUnit.MILLISECONDS);
    }

    private void doLogAction(String tenantId, RequestContext context, String action, Object data, Duration timeElapsed, Throwable error) {
        TenantHolder.use(tenantId, () -> {
            try {
                final io.soffa.foundation.core.data.entities.SysLog log = new io.soffa.foundation.core.data.entities.SysLog();
                log.setKind("action");
                log.setEvent(action);
                if (data != null) {
                    log.setData(JsonUtil.serialize(data));
                }
                log.setContext(context);
                log.setError(error);
                log.setDuration(timeElapsed.toMillis());
                sysLogs.save(log);
            } catch (Exception e) {
                //TODO: MemSave or FileSave to retry later
                LOG.error(e, "Failed to save sys log event: %s", e.getMessage());
            }
        });
    }


}
