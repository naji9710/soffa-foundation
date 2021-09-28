package io.soffa.foundation;

public interface Action<I, O> {

     O handle(I request, RequestContext context);

}
