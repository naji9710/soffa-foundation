package io.soffa.foundation.actions;

import io.soffa.foundation.commons.ErrorUtil;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.model.TenantId;
import io.soffa.foundation.core.model.Validatable;
import io.soffa.foundation.data.SysLog;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.events.Event;
import io.soffa.foundation.exceptions.ManagedException;
import io.soffa.foundation.exceptions.TechnicalException;
import org.apache.commons.lang.reflect.MethodUtils;

import javax.transaction.Transactional;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class DefaultActionDispatcher implements ActionDispatcher {

    private static final Logger LOG = Logger.get(DefaultActionDispatcher.class);
    private final Set<Action<?, ?>> registry;
    private final Set<Action0<?>> registry0;
    private final Map<String, Object> actionsMapping = new HashMap<>();
    private final Map<String, Class<?>> inputTypes = new HashMap<>();
    private final SysLogRepository sysLogs;
    private static final ScheduledExecutorService SC = Executors.newScheduledThreadPool(16);


    public DefaultActionDispatcher(Set<Action<?, ?>> registry,
                                   Set<Action0<?>> registry0,
                                   SysLogRepository sysLogs) {
        this.registry = registry;
        this.registry0 = registry0;
        this.sysLogs = sysLogs;
        register(registry);
        register(registry0);
    }

    private void register(Set<?> actions) {
        for (Object action : actions) {
            actionsMapping.put(action.getClass().getSimpleName(), action);
            for (Class<?> intf : action.getClass().getInterfaces()) {
                if (Action0.class.isAssignableFrom(intf)) {
                    actionsMapping.put(intf.getSimpleName(), action);
                } else if (Action.class.isAssignableFrom(intf)) {
                    actionsMapping.put(intf.getSimpleName(), action);
                    Method method = Arrays.stream(action.getClass().getMethods())
                        .filter(m -> "handle".equals(m.getName()) && 2 == m.getParameterCount() && m.getParameterTypes()[1] == RequestContext.class)
                        .findFirst().orElseThrow(() -> new TechnicalException("Invalid action definition"));
                    inputTypes.put(intf.getSimpleName(), method.getParameterTypes()[0]);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request) {
        RequestContext context = RequestContextHolder.require();
        return dispatch(actionClass, request, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O dispatch(Class<? extends Action<I, O>> actionClass, I request, RequestContext context) {
        if (request instanceof Validatable) {
            ((Validatable) request).validate();
        }
        for (Action<?, ?> act : registry) {
            if (actionClass.isAssignableFrom(act.getClass())) {
                Action<I, O> impl = (Action<I, O>) act;
                return logAction(actionClass.getSimpleName(), request, context, () -> impl.handle(request, context));
            }
        }
        throw new TechnicalException("Unable to find implementation for action: %s", actionClass.getName());
    }

    @Override
    public <O> O dispatch(Class<? extends Action0<O>> actionClass) {
        RequestContext context = RequestContextHolder.require();
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
        throw new TechnicalException("Unable to find implementation for action: %s", actionClass.getName());
    }

    @Override
    public void handle(Event event) {
        Object action = actionsMapping.get(event.getAction());
        if (action == null) {
            LOG.error("No action handle found to event %s, dont't forget to use the action simple class name");
            return;
        }
        if (action instanceof Action) {
            Class<?> inputType = inputTypes.get(event.getAction());
            Object payload = event.getPayloadAs(inputType).orElse(null);
            logAction(action.getClass().getSimpleName(), payload, event.getContext(), () -> {
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
        final AtomicReference<Throwable> error = new AtomicReference<>(null);
        try {
            return supplier.get();
        } catch (Exception e) {
            LOG.error("Action %s has failed with message %s", action, ErrorUtil.loookupOriginalMessage(e));
            error.set(e);
            if (ManagedException.class.isAssignableFrom(e.getClass())) {
                throw e;
            }
            throw new TechnicalException(e.getMessage(), e);
        } finally {

            if (sysLogs != null) {

                Instant finish = Instant.now();
                Duration timeElapsed = Duration.between(start, finish);
                if (timeElapsed.getSeconds() >= SLOW_ACTION_THRESHOLD) {
                    LOG.warn("action %s tooks more than %ds", action, timeElapsed.getSeconds());
                }
                final TenantId tenantId = context.getTenantId();
                SC.schedule(() -> {
                    TenantHolder.use(tenantId, () -> {
                        try {
                            final SysLog log = new SysLog();
                            log.setKind("action");
                            log.setEvent(action);
                            if (data != null) {
                                log.setData(JsonUtil.serialize(data));
                            }
                            log.setContext(context);
                            log.setError(error.get());
                            log.setDuration(timeElapsed.toMillis());
                            // log.setId(IdGenerator.shortUUID("slog_"));
                            // log.setCreatedAt(new Date());
                            sysLogs.save(log);
                        } catch (Exception e) {
                            LOG.error(e, "failed to save sys log event: %s", e.getMessage());
                        }
                    });
                }, 100, TimeUnit.MILLISECONDS);
            }
        }
    }


}
