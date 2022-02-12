package io.soffa.foundation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Authentication   {

    private String application;
    private String username;
    private UserProfile profile;
    private TenantId tenantId;
    private Set<String> roles;
    private Set<String> groups;
    private Set<String> permissions;
    private Map<String, Object> claims;
    private Map<String, Object> originalClaims;
    private Object principal;
    private boolean liveMode;

    public boolean hasPermission(String id) {
        return permissions != null && permissions.contains(id);
    }


}
