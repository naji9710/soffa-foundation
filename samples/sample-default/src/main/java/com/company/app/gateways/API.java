package com.company.app.gateways;

import com.company.app.core.Echo;
import com.company.app.core.Ping;
import com.company.app.core.PingResponse;
import io.soffa.foundation.annotations.BindOperation;
import io.soffa.foundation.application.RequestContext;
import io.soffa.foundation.exposition.ApiHeaders;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import javax.ws.rs.Path;

@Tags(
    @Tag(name = "app", description = "Value application tag")
)
public interface API {

    @io.swagger.v3.oas.annotations.Operation(
        method = "GET",
        summary = "Ping endpoint",
        description = "Will return pong message on successful request",
        parameters = {@Parameter(ref = ApiHeaders.TENANT_ID)}
    )
    @Path("/ping")
    @BindOperation(Ping.class)
    PingResponse ping(@Parameter(hidden = true) RequestContext context);

    @io.swagger.v3.oas.annotations.Operation(
        method = "POST",
        summary = "Echo endpoint",
        description = "Will return the sent message",
        parameters = {@Parameter(ref = ApiHeaders.TENANT_ID)}
    )
    @Path("/echo")
    @BindOperation(Echo.class)
    String echo(String input, @Parameter(hidden = true) RequestContext context);

}
