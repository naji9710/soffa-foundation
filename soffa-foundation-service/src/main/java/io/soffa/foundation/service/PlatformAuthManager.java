package io.soffa.foundation.service;

import com.google.common.collect.ImmutableSet;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.model.Authentication;
import io.soffa.foundation.security.AuthManager;
import io.soffa.foundation.security.roles.GrantedRole;
import io.soffa.foundation.tokens.TokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
@AllArgsConstructor
public class PlatformAuthManager {

    private static final Logger LOG = Logger.get(PlatformAuthManager.class);
    private final TokenProvider tp;
    private final AuthManager authManger;

    private Authentication authenticate(RequestContext context, String token) {
        Authentication auth = authManger.authenticate(context, token);
        if (auth != null) {
            return auth;
        }
        if (tp == null) {
            return null;
        }
        return tp.decode(token);
    }

    private Authentication authenticate(RequestContext context, String username, String password) {
        return authManger.authenticate(context, username, password);
    }

    public boolean isEnabled() {
        return tp.getConfig().isActive();
    }

    public boolean isServiceAuthorization(String token) {
        return token.equals(tp.getConfig().getServiceToken());
    }

    public void process(RequestContext context) {
        process(context, context.getAuthorization());
    }

    public void process(RequestContext context, String token) {
        Authentication auth = null;

        if (token.toLowerCase().startsWith("bearer ")) {
            String lToken = token.substring("bearer ".length()).trim();
            LOG.debug("Bearer authorization header found: %s", lToken);
            auth = authenticate(context, lToken);
        } else if (token.toLowerCase().startsWith("basic ")) {
            String basicAuth = token.substring("basic ".length()).trim();
            String[] credentials = new String(Base64.getDecoder().decode(basicAuth)).split(":");
            String username = credentials[0];
            String pasword = "";
            boolean hasPassword = credentials.length > 1;
            if (hasPassword) {
                pasword = credentials[1];
            }
            if (isServiceAuthorization(pasword)) {
                auth = Authentication.builder()
                    .username(username)
                    .principal(username)
                    .permissions(ImmutableSet.of("service"))
                    .roles(ImmutableSet.of("service"))
                    .build();
            } else {
                auth = authenticate(context, username, pasword);
            }
        }

        if (auth == null) {
            return;
        }

        context.setAuthentication(auth);
        context.setAuthorization(token);
        List<GrantedAuthority> permissions = createPermissions(context, auth);
        UsernamePasswordAuthenticationToken authz = new UsernamePasswordAuthenticationToken(context, null, permissions);
        SecurityContextHolder.getContext().setAuthentication(authz);
    }

    private List<GrantedAuthority> createPermissions(RequestContext context, Authentication auth) {
        List<GrantedAuthority> permissions = new ArrayList<>();
        permissions.add(new SimpleGrantedAuthority(GrantedRole.USER));
        permissions.add(new SimpleGrantedAuthority(GrantedRole.AUTHENTICATED));
        if (TextUtil.isNotEmpty(context.getApplicationName())) {
            permissions.add(new SimpleGrantedAuthority(GrantedRole.HAS_APPLICATION));
        }
        if (context.getTenantId() != null) {
            permissions.add(new SimpleGrantedAuthority(GrantedRole.HAS_TENANT_ID));
        }
        if (auth.getRoles() != null) {
            for (String role : auth.getRoles()) {
                if (TextUtil.isNotEmpty(role)) {
                    permissions.add(new SimpleGrantedAuthority(role.trim()));
                }
            }
        }
        if (auth.getPermissions() != null) {
            for (String permission : auth.getPermissions()) {
                if (TextUtil.isNotEmpty(permission)) {
                    permissions.add(new SimpleGrantedAuthority(permission.trim()));
                }
            }
        }
        return permissions;
    }

}
