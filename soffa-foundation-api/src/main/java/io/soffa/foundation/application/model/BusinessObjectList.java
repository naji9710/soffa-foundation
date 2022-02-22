package io.soffa.foundation.application.model;

import io.soffa.foundation.annotations.JsonModel;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@JsonModel
public class BusinessObjectList<T> {

    private final String object = "list";
    private Map<String, Object> metadata;
    private final List<T> data;

    public BusinessObjectList(List<T> data, Map<String, Object> metadata) {
        this.metadata = metadata;
        this.data = data;
    }

    public BusinessObjectList(List<T> data) {
        this.data = data;
    }
}
