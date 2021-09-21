package io.soffa.commons.logging;

import com.mgnt.utils.TextUtils;
import io.soffa.commons.exceptions.Errors;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class Logger {

    private final org.slf4j.Logger logger;

    public Logger(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public void debug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, args);
        }
    }

    public void trace(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.trace(message, args);
        }
    }

    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public void error(Throwable e) {
        error(Errors.loookupOriginalMessage(e), e);
    }

    public void error(Throwable error, String message, Object... args) {
        error(MessageFormat.format(message, args), error);
    }

    public void error(String message, Throwable e) {
        logger.error(message);
        logger.error(Errors.getStacktrace(e));
    }

    public void error(String message, Object... args) {
        logger.error(message, args);
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
