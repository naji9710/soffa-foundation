package io.soffa.foundation.commons.logging;

import com.mgnt.utils.TextUtils;
import io.soffa.foundation.commons.exceptions.ErrorUtil;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class Logger {

    private final org.slf4j.Logger log;

    public Logger(org.slf4j.Logger logger) {
        this.log = logger;
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
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
        error(MessageFormat.format(message, args), error);
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
