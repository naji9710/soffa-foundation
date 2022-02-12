package io.soffa.foundation.service.config;

import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.context.RequestContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SecurityControllerAdvice {

    @ModelAttribute
    public RequestContext createRequestContextAttribute() {
        return RequestContextHolder.get().orElse(null);
    }

}
