package io.soffa.foundation.app.core;

import io.soffa.foundation.core.RequestContext;

import javax.inject.Named;


@Named
public class EchoActionImpl implements EchoAction {

    @Override
    public String handle(String request, RequestContext context) {
        return request;
    }
}
