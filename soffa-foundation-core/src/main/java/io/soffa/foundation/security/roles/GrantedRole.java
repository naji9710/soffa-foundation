package io.soffa.foundation.security.roles;

public interface GrantedRole {

    String USER = "user";
    String AUTHENTICATED = "authenticated";
    String HAS_APPLICATION = "ctx-application";
    String HAS_TENANT_ID = "ctx-tenant";

}
