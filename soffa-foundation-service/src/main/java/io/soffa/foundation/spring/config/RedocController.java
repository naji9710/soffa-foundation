package io.soffa.foundation.spring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@ConditionalOnProperty(value = "app.redoc.enabled", havingValue = "true")
public class RedocController {

    @GetMapping("/")
    public String redocHome() {
        return "redoc";
    }

}
