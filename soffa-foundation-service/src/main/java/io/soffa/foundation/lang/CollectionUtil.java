package io.soffa.foundation.lang;

import java.util.Collection;

public final class CollectionUtil {

    private CollectionUtil() {
    }

    public static <T> boolean isNotEmpty(Collection<T> list) {
        return !isEmpty(list);
    }

    public static <T> boolean isEmpty(Collection<T> list) {
        return list == null || list.isEmpty();
    }
}
