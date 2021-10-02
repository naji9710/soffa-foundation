package io.soffa.foundation.core;

public interface Action<I, O> {

     O handle(I request, RequestContext context);

}
