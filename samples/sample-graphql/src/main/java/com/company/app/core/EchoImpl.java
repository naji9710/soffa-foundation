package com.company.app.core;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import io.soffa.foundation.core.RequestContext;
import org.jetbrains.annotations.NotNull;

import javax.inject.Named;


@Named
@DgsComponent
public class EchoImpl implements Echo {

    @Override
    @DgsQuery(field = "echo")
    public String handle(@InputArgument @NotNull String request, @NotNull RequestContext context) {
        return request;
    }

}
