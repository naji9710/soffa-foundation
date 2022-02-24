package io.soffa.foundation.core.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.AppConfig;
import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.core.TenantsLoader;
import io.soffa.foundation.core.context.DefaultRequestContext;
import io.soffa.foundation.core.models.TenantList;
import io.soffa.foundation.core.operation.GetTenantList;
import io.soffa.foundation.core.pubsub.PubSubClientFactory;
import io.soffa.foundation.core.pubsub.PubSubMessenger;
import io.soffa.foundation.core.security.TokenProvider;
import io.soffa.foundation.models.Token;
import io.soffa.foundation.models.TokenType;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class PubSubTenantsLoader implements TenantsLoader {

    private static final Logger LOG = Logger.get(TenantsLoader.class);

    private final PubSubMessenger client;
    private final TokenProvider tokens;
    private final AppConfig app;
    private final String serviceId;
    private final String permissions;

    @Override
    public Set<String> getTenantList() {
        GetTenantList operation = PubSubClientFactory.of(GetTenantList.class, serviceId, client);
        try {
            Token token = tokens.create(TokenType.JWT, app.getName(), ImmutableMap.of("permissions", permissions));
            RequestContext context = new DefaultRequestContext().withAuthorization("Bearer " + token.getValue());
            TenantList res = operation.handle(null, context);
            if (res == null || res.getTenants() == null) {
                LOG.warn("Call to service %s returned an empty tenants list.", serviceId);
                return ImmutableSet.of();
            }
            LOG.warn("Call to service %s returned %d tenant(s).", serviceId, res.getTenants().size());
            return res.getTenants();
        } catch (Exception e) {
            LOG.error("Unable to fetch tenants list from bantu-accounts, make sure the service is reachable", e);
            return ImmutableSet.of();
        }
    }
}
