package io.soffa.foundation.core.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class TenantId {

    @JsonValue
    String value;

}
