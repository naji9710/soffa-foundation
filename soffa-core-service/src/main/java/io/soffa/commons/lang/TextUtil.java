package io.soffa.commons.lang;

import org.apache.commons.lang3.StringUtils;

public class TextUtil {

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static String trimToEmpty(String schema) {
        return StringUtils.trimToEmpty(schema);
    }
}
