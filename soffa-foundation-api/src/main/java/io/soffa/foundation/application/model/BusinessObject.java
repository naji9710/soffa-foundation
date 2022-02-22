package io.soffa.foundation.application.model;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

@Getter
public class BusinessObject<T> {

    private final String object;
    private final Map<String, Object> metadata;
    private final T data;

    public BusinessObject(@NonNull String object, T data) {
        this(object, data, null);
    }

    public BusinessObject(@NonNull String object, T data, Map<String, Object> metadata) {
        this.object = object;
        this.metadata = metadata;
        this.data = data;
    }
}
