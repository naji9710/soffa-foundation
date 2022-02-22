package io.soffa.foundation.service.core.config;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.context.RequestContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SecurityControllerAdvice {

    @ModelAttribute
    public RequestContext createRequestContextAttribute() {
        return RequestContextHolder.get().orElse(null);
    }

}
