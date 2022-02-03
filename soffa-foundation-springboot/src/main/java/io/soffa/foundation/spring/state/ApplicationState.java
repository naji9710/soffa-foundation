package io.soffa.foundation.spring.state;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
public class ApplicationState {

    private boolean ready;
    private Map<String, State> states;

}
