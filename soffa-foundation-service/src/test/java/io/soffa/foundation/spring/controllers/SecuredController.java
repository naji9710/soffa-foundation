package io.soffa.foundation.spring.controllers;

import io.soffa.foundation.core.annotations.ApplicationRequired;
import io.soffa.foundation.core.annotations.Authenticated;
import io.soffa.foundation.core.annotations.TenantRequired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Authenticated
public class SecuredController {

    @GetMapping("/secure")
    public String ping() {
        return "Secured";
    }

    @ApplicationRequired
    @TenantRequired
    @GetMapping("/secure/full")
    public String pong() {
        return "Secured";
    }

}
