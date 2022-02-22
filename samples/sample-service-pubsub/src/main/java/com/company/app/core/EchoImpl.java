package com.company.app.core;

import io.soffa.foundation.core.RequestContext;
import org.jetbrains.annotations.NotNull;

import javax.inject.Named;


@Named
public class EchoImpl implements Echo {

    @Override
    public String handle(@NotNull String input, @NotNull RequestContext context) {
        return input;
    }

}
