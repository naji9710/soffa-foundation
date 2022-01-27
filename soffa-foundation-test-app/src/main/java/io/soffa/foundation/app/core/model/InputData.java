package io.soffa.foundation.app.core.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class InputData {

    @NotEmpty
    private String username;
    
}
