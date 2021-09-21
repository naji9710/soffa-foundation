package io.soffa.service.core;

public interface Action<I, O> {

     O handle(I request, RequestContext context);

}
