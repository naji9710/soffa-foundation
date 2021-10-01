package io.soffa.foundation.actions;

import io.soffa.foundation.core.Action;
import io.soffa.foundation.core.Action0;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.Validatable;
import io.soffa.foundation.data.SysLog;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.exceptions.ErrorUtil;
import io.soffa.foundation.exceptions.TechnicalException;
import io.soffa.foundation.logging.Logger;
import io.soffa.foundation.pubsub.Event;
import io.soffa.foundation.support.ClassUtil;
import io.soffa.foundation.support.JsonUtil;
import org.apache.commons.lang.reflect.MethodUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class DefaultActionDispatcher implements ActionDispatcher {

    private static final Logger LOG = Logger.create("app");
    private final Set<Action<?, ?>> registry;
    private final Set<Action0<?>> registry0;
    private final Map<String, Object> actionsMapping = new HashMap<>();
    private final SysLogRepository sysLogs;

    public DefaultActionDispatcher(Set<Action<?, ?>> registry, Set<Action0<?>> registry0, SysLogRepository sysLogs) {

        this.registry = registry;
        this.registry0 = registry0;
        this.sysLogs = sysLogs;

        for (Action<?, ?> action : registry) {
            actionsMapping.put(action.getClass().getSimpleName(), action);
            for (Class<?> intf : action.getClass().getInterfaces()) {
                if (Action.class.isAssignableFrom(intf)) {
                    actionsMapping.put(intf.getSimpleName(), action);
                }
            }
        }
        for (Action0<?> action : registry0) {
            actionsMapping.put(action.getClass().getSimpleName(), action);
            for (Class<?> intf : action.getClass().getInterfaces()) {
                if (Action0.class.isAssignableFrom(intf)) {
                    actionsMapping.put(intf.getSimpleName(), action);
                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends Validatable, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request) {
        RequestContext context = (RequestContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return dispatch(actionClass, request, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends Validatable, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request, RequestContext context) {
        request.validate();
        for (Action<?, ?> act : registry) {
            if (actionClass.isAssignableFrom(act.getClass())) {
                Action<I, O> impl = (Action<I, O>) act;
                return logAction(actionClass.getSimpleName(), request, context, () -> impl.handle(request, context));
            }
        }
        throw new TechnicalException("Unable to find implementation for action: {0}", actionClass.getName());
    }

    @Override
    public <O> O dispatch(Class<? extends Action0<O>> actionClass) {
        RequestContext context = (RequestContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return dispatch(actionClass, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public <O> O dispatch(Class<? extends Action0<O>> actionClass, RequestContext context) {
        for (Action0<?> act : registry0) {
            if (actionClass.isAssignableFrom(act.getClass())) {
                Action0<O> impl = (Action0<O>) act;
                return logAction(actionClass.getSimpleName(), null, context, () -> impl.handle(context));
            }
        }
        throw new TechnicalException("Unable to find implementation for action: {0}", actionClass.getName());
    }

    @Override
    public void handle(Event event) throws MissingEventHandlerException {
        Object action = actionsMapping.get(event.getAction());
        if (action == null) {
            throw new MissingEventHandlerException();
        }
        if (action instanceof Action) {
            Class<?> inputType = ClassUtil.getClassFromGenericInterface(action.getClass(), Validatable.class, 0);
            Object payload = event.getPayloadAs(inputType).orElse(null);
            if (payload == null) {
                LOG.warn("Invalid messaging payload received. Event={}", event.getPayload());
                return;
            }
            logAction(action.getClass().getSimpleName(), null, event.getContext(), () -> {
                try {
                    return MethodUtils.invokeMethod(action, "handle", new Object[]{payload, event.getContext()});
                } catch (Exception e) {
                    throw new TechnicalException(e.getMessage(), e);
                }
            });
            return;
        }
        logAction(action.getClass().getSimpleName(), null, event.getContext(), () -> ((Action0<?>) action).handle(event.getContext()));
    }


    private <O> O logAction(String action, Object data, RequestContext context, Supplier<O> supplier) {
        Instant start = Instant.now();
        Throwable error = null;
        try {
            return supplier.get();
        } catch (Exception e) {
            LOG.error(e, "action {0} has failed with message {1}", action, ErrorUtil.loookupOriginalMessage(e));
            error = e;
            throw e;
        } finally {
            if (sysLogs != null || LOG.isInfoEnabled()) {
                // Should be ran in background
                Instant finish = Instant.now();
                Duration timeElapsed = Duration.between(start, finish);
                if (timeElapsed.getSeconds() >= 3) {
                    LOG.warn("action {O} tooks more than {1}s", action, timeElapsed.getSeconds());
                }
                if (sysLogs != null) {
                    try {
                        SysLog log = new SysLog();
                        log.setKind("action");
                        log.setEvent(action);
                        if (data != null) {
                            log.setData(JsonUtil.serialize(data));
                        }
                        if (context != null) {
                            log.setRequestId(context.getRequestId());
                            log.setSpanId(context.getSpanId());
                            log.setTraceId(context.getTraceId());
                            log.setUser(context.getUsername().orElse("guest"));
                            log.setApplication(context.getApplicationName());
                            if (error != null) {
                                log.setError(ErrorUtil.loookupOriginalMessage(error));
                                log.setErrorDetails(ErrorUtil.getStacktrace(error));
                            }
                        }
                        log.setDuration(timeElapsed.toMillis());
                        sysLogs.save(log);

                    } catch (Exception e) {
                        LOG.error("failed to persist syslog", e);
                    }
                }
            }
        }
    }


}
