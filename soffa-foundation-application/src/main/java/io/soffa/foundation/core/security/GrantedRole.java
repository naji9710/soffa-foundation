package io.soffa.foundation.core.security;

public interface GrantedRole {

    String USER = "user";
    String AUTHENTICATED = "authenticated";
    String HAS_APPLICATION = "ctx-application";
    String HAS_TENANT_ID = "ctx-tenant";

}
