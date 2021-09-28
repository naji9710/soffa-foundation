package io.soffa.foundation.commons.exceptions;

import com.mgnt.utils.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public final class ErrorUtil {

    private static String defaultErrorPackage
            = "*";
    private static final String ALL_PACKAGES = "*";

    private ErrorUtil() {}

    public static void setRelevantPackage(String pkg) {
        defaultErrorPackage = pkg;
        if (!ALL_PACKAGES.equals(pkg)) {
            TextUtils.setRelevantPackage(pkg);
        }
    }

    public static String getStacktrace(Throwable e){
        if ("*".equals(defaultErrorPackage)) {
            return TextUtils.getStacktrace(e, true);
        }
        return TextUtils.getStacktrace(e, true, defaultErrorPackage);
    }

    public static Throwable unwrap(Throwable error) {
        if (error instanceof InvocationTargetException && error.getCause() != null) {
            return unwrap(error.getCause());
        }
        if (error instanceof UndeclaredThrowableException && error.getCause() != null) {
            return unwrap(error.getCause());
        }
        if (error instanceof RuntimeException && error.getCause() != null) {
            return unwrap(error.getCause());
        }
        return error;
    }

    public static String loookupOriginalMessage(Throwable error) {
        if (error instanceof TechnicalException || error instanceof FunctionalException) {
            return error.getMessage();
        }
        if (error.getCause() != null) {
            return loookupOriginalMessage(error.getCause());
        }
        return error.getMessage();
    }
}
