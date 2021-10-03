package io.soffa.foundation.lang;

import org.apache.commons.lang3.StringUtils;

public final class TextUtil {

    private TextUtil(){}

    public static boolean isEmpty(String value) {
        return StringUtils.isEmpty(value);
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static String trimToEmpty(String schema) {
        return StringUtils.trimToEmpty(schema);
    }

    public static String format(String pattern, Object... args) {
        if (args==null ||args.length==0) {
            return pattern;
        }
        return String.format(pattern, args);
    }

}
