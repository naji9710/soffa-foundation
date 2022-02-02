package com.company.app.core;

import io.soffa.foundation.annotations.SysLog;
import io.soffa.foundation.core.RequestContext;
import org.jetbrains.annotations.NotNull;

import javax.inject.Named;


@Named
public class EchoActionImpl implements EchoAction {

    @Override
    @SysLog()
    public String handle(@NotNull String request, @NotNull RequestContext context) {
        return request;
    }
}
