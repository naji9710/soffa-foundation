package io.soffa.foundation.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class TenantId {

    @JsonValue
    String value;

}
