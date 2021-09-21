package io.soffa.commons.exceptions;

import com.mgnt.utils.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class Errors {

    private Errors() {}

    private static String DEFAULT_ERROR_PACKAGE = "*";

    public static void setRelevantPackage(String pkg) {
        DEFAULT_ERROR_PACKAGE = pkg;
        if (!"*".equals(pkg)) {
            TextUtils.setRelevantPackage(pkg);
        }
    }

    public static String getStacktrace(Throwable e){
        if ("*".equals(DEFAULT_ERROR_PACKAGE)) {
            return TextUtils.getStacktrace(e, true);
        }
        return TextUtils.getStacktrace(e, true, DEFAULT_ERROR_PACKAGE);
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
