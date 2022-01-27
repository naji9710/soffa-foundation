package io.soffa.foundation.app.core;

import io.soffa.foundation.core.RequestContext;
import org.jetbrains.annotations.NotNull;

import javax.inject.Named;


@Named
public class EchoActionImpl implements EchoAction {

    @Override
    public String handle(@NotNull String request, @NotNull RequestContext context) {
        return request;
    }
}
