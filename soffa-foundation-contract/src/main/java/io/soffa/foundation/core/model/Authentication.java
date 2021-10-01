package io.soffa.foundation.core.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class Authentication {

    private String application;
    private String username;
    private UserProfile profile;
    private TenantId tenantId;
    private Set<String> roles;
    private Set<String> groups;
    private Set<String> permissions;

}
