package com.company.app.gateways;

import com.company.app.core.EchoAction;
import com.company.app.core.PingAction;
import com.company.app.core.PingResponse;
import io.soffa.foundation.core.ApiHeaders;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.annotations.BindAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import javax.ws.rs.Path;

@Tags(
    @Tag(name = "app", description = "Value application tag")
)
public interface API {

    @Operation(
        method = "GET",
        summary = "Ping endpoint",
        description = "Will return pong message on successful request",
        parameters = {@Parameter(ref = ApiHeaders.TENANT_ID)}
    )
    @Path("/ping")
    @BindAction(PingAction.class)
    PingResponse ping(@Parameter(hidden = true) RequestContext context);

    @Operation(
        method = "POST",
        summary = "Echo endpoint",
        description = "Will return the sent message",
        parameters = {@Parameter(ref = ApiHeaders.TENANT_ID)}
    )
    @Path("/echo")
    @BindAction(EchoAction.class)
    String echo(String input, @Parameter(hidden = true) RequestContext context);

}
