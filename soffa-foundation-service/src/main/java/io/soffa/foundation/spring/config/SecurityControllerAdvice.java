package io.soffa.foundation.spring.config;

import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.core.RequestContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SecurityControllerAdvice {

    @ModelAttribute
    public RequestContext createRequestContextAttribute() {
        return RequestContextHolder.get().orElse(null);
    }

}
