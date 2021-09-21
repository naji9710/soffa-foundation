package io.soffa.spring.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.soffa.commons.annotations.*;

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
