package io.soffa.service.core.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class TenantId {

    @JsonValue
    String value;

}
