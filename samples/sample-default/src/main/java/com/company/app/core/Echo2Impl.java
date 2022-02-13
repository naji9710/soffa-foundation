package com.company.app.core;

import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.model.Ack;
import io.soffa.foundation.model.NoInput;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Named;


@Named
public class Echo2Impl implements Echo2 {

    @Override
    public Ack handle(@NonNull NoInput input, @NonNull RequestContext context) {
        return Ack.OK;
    }

}
