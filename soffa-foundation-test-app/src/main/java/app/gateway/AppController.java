package app.gateway;

import app.core.PingAction;
import io.soffa.foundation.core.ApiHeaders;
import io.soffa.foundation.actions.ActionDispatcher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Tags(
    @Tag(name = "app", description = "Default application tag")
)
public class AppController {

    private final ActionDispatcher dispatcher;
    @Operation(
        summary = "Ping endpoint",
        description = "Will return pong message on successful request",
        parameters = {@Parameter(ref = ApiHeaders.TENANT_ID)}
    )
    @GetMapping("ping")
    public String ping() {
        return dispatcher.dispatch(PingAction.class);
    }

}
