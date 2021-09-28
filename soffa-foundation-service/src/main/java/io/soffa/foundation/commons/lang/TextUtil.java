package io.soffa.foundation.commons.lang;

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

}
