package com.company.app.gateways;

import com.company.app.core.Echo;
import com.company.app.core.Ping;
import com.company.app.core.PingResponse;
import com.company.app.core.model.InputData;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.operations.OperationDispatcher;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Validated
public class AppController implements API {

    private final OperationDispatcher dispatcher;

    @Override
    @GetMapping("ping")
    public PingResponse ping(RequestContext context) {
        return dispatcher.dispatch(Ping.class, context);
    }

    @Override
    @PostMapping("echo")
    public String echo(@RequestBody String input, RequestContext context) {
        return dispatcher.dispatch(Echo.class, input, context);
    }

    @PostMapping("check")
    public String check(@Valid @RequestBody InputData input) {
        return input.getUsername();
    }

}
