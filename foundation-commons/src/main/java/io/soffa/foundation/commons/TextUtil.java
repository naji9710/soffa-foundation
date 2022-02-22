package io.soffa.foundation.commons;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

public final class TextUtil {

    private TextUtil() {
    }

    public static boolean isEmpty(String... values) {
        for (String value : values) {
            if (StringUtils.isNotEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotEmpty(String... value) {
        for (String s : value) {
            if (StringUtils.isEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    public static String trimToEmpty(String schema) {
        return StringUtils.trimToEmpty(schema);
    }

    public static String trimToNull(String schema) {
        return StringUtils.trimToNull(schema);
    }

    public static String format(final String pattern, Object... args) {
        if (args == null || args.length == 0) {
            return pattern;
        }
        if (pattern.contains("{}") && !pattern.contains("%")) {
            return MessageFormat.format(pattern, args);
        }
        return String.format(pattern, args);
    }

}
