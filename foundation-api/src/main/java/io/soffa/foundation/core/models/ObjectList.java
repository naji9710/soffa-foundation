package io.soffa.foundation.core.models;

import io.soffa.foundation.annotations.JsonModel;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@JsonModel
public class ObjectList<T extends ObjectModel> extends ObjectModel {

    private final List<T> data;
    private final Boolean hasMore;

    public ObjectList(List<T> data) {
        this(data, (Boolean) null);
    }

    public ObjectList(List<T> data, Boolean hasMore) {
        super("list");
        this.data = data;
        this.hasMore = hasMore;
    }

    public ObjectList(List<T> data, Map<String, Object> metadata) {
        this(data, (Boolean) null);
        super.setMetadata(metadata);
    }
}
