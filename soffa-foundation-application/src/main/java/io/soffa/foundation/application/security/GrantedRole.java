package io.soffa.foundation.application.security;

public interface GrantedRole {

    String USER = "user";
    String AUTHENTICATED = "authenticated";
    String HAS_APPLICATION = "ctx-application";
    String HAS_TENANT_ID = "ctx-tenant";

}
