package io.soffa.spring.controllers;

import io.soffa.foundation.commons.annotations.ApplicationRequired;
import io.soffa.foundation.commons.annotations.Authenticated;
import io.soffa.foundation.commons.annotations.TenantRequired;
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
