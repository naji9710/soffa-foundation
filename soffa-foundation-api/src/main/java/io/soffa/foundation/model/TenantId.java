package io.soffa.foundation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.soffa.foundation.commons.TextUtil;
import lombok.Value;

@Value
public class TenantId   {

    public static final String DEFAULT_VALUE = "default";
    public static final TenantId DEFAULT = new TenantId(DEFAULT_VALUE);

    @JsonValue
    String value;

    @JsonCreator
    public TenantId(String value) {
        this.value = value;
    }

    public static TenantId of(String value) {
        if (TextUtil.isEmpty(value)) {
            return null;
        }
        return new TenantId(value);
    }

    @Override
    public String toString() {
        return value;
    }

}


