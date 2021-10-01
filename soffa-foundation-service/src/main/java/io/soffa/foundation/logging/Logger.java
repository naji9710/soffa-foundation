package io.soffa.foundation.logging;

import com.mgnt.utils.TextUtils;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.exceptions.ErrorUtil;
import io.soffa.foundation.lang.TextUtil;
import org.slf4j.LoggerFactory;

public class Logger {

    private final org.slf4j.Logger log;
    static {
        Logger.setRelevantPackage("app");
    }

    public Logger(org.slf4j.Logger logger) {
        this.log = logger;
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public static void setContext(RequestContext context) {
        org.slf4j.MDC.setContextMap(context.getContextMap());
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public void debug(String message, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(message, args);
        }
    }

    public void trace(String message, Object... args) {
        if (log.isDebugEnabled()) {
            log.trace(message, args);
        }
    }

    public void info(String message, Object... args) {
        log.info(message, args);
    }

    public void warn(String message, Object... args) {
        log.warn(message, args);
    }

    public void error(Throwable e) {
        error(ErrorUtil.loookupOriginalMessage(e), e);
    }

    public void error(Throwable error, String message, Object... args) {
        error(TextUtil.format(message, args), error);
    }

    public void error(String message, Throwable e) {
        log.error(message);
        log.error(ErrorUtil.getStacktrace(e));
    }

    public void error(String message, Object... args) {
        log.error(message, args);
    }

    public static void setRelevantPackage(String pkg) {
        if ("*".equals(pkg)) {
            TextUtils.setRelevantPackage(null);
        } else {
            TextUtils.setRelevantPackage(pkg);
        }
    }

    public static Logger create(Class<?> type) {
        return new Logger(LoggerFactory.getLogger(type));
    }

    public static Logger create(String name) {
        return new Logger(LoggerFactory.getLogger(name));
    }

}
