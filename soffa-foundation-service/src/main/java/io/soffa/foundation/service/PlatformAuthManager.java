package io.soffa.foundation.service;

import com.google.common.collect.ImmutableSet;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.context.RequestContext;
import io.soffa.foundation.model.Authentication;
import io.soffa.foundation.security.AuthManager;
import io.soffa.foundation.security.GrantedRole;
import io.soffa.foundation.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class PlatformAuthManager {

    private static final Logger LOG = Logger.get(PlatformAuthManager.class);
    private final TokenProvider tokens;
    private final AuthManager authManger;

    public PlatformAuthManager(@Autowired AuthManager authManger, @Autowired(required = false) TokenProvider tokens) {
        this.tokens = tokens;
        this.authManger = authManger;
    }

    private Authentication authenticate(RequestContext context, String token) {
        Authentication auth = authManger.authenticate(context, token);
        if (auth != null) {
            return auth;
        }
        if (tokens == null) {
            return null;
        }
        return tokens.decode(token);
    }

    private Authentication authenticate(RequestContext context, String username, String password) {
        return authManger.authenticate(context, username, password);
    }

    public void handle(RequestContext context) {
        handle(context, context.getAuthorization());
    }

    public void handle(RequestContext context, String token) {

        if (TextUtil.isEmpty(token)) {
            return;
        }
        Authentication auth = null;

        if (token.toLowerCase().startsWith("bearer ")) {
            String lToken = token.substring("bearer ".length()).trim();
            LOG.debug("Bearer authorization header found: %s", lToken);
            auth = authenticate(context, lToken);
        } else if (token.toLowerCase().startsWith("basic ")) {
            String basicAuth = token.substring("basic ".length()).trim();
            String[] credentials = new String(Base64.getDecoder().decode(basicAuth)).split(":");
            String username = credentials[0];
            boolean hasPassword = credentials.length > 1;
            String pasword = hasPassword ? credentials[1] : "";
            if (tokens!=null && pasword.equals(tokens.getConfig().getSecret())) {
                auth = Authentication.builder()
                    .application(username)
                    //.username(username)
                    .tenantId(context.getTenantId())
                    .principal(username)
                    .permissions(ImmutableSet.of("service"))
                    .roles(ImmutableSet.of("service"))
                    .build();
            } else {
                auth = authenticate(context, username, pasword);
            }
        } else {
            LOG.warn("An authorization header was found but it is not a bearer or basic authorization header");
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
