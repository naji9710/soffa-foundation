package io.soffa.foundation.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class ObjectModel {

    @JsonProperty("_object")
    private String object;
    @JsonProperty("_metadata")
    private Map<String, Object> metadata;

    private ObjectModel() {
    }

    public ObjectModel(String object) {
        this.object = object;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
