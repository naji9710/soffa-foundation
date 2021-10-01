package io.soffa.foundation.core;

public interface Action<I extends Validatable, O> {

     O handle(I request, RequestContext context);

}
