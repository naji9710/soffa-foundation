package io.soffa.foundation.app.gateway;

import io.soffa.foundation.actions.ActionDispatcher;
import io.soffa.foundation.app.core.EchoAction;
import io.soffa.foundation.app.core.PingAction;
import io.soffa.foundation.app.core.PingResponse;
import io.soffa.foundation.app.core.model.InputData;
import io.soffa.foundation.core.RequestContext;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class AppController implements API {

    private final ActionDispatcher dispatcher;

    @Override
    @GetMapping("ping")
    public PingResponse ping(RequestContext context) {
        return dispatcher.dispatch(PingAction.class, context);
    }

    @Override
    @PostMapping("echo")
    public String echo(@RequestBody String input, RequestContext context) {
        return dispatcher.dispatch(EchoAction.class, input, context);
    }

    @PostMapping("check")
    public String check(@Valid @RequestBody InputData input) {
        return input.getUsername();
    }

}
