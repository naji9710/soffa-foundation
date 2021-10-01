package io.soffa.foundation.web;

import com.google.common.base.Preconditions;
import io.soffa.foundation.lang.CollectionUtil;
import io.soffa.foundation.lang.TextUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.Optional;

public class OpenApiBuilder {

    private final Components components = new Components();
    private final OpenAPIDesc desc;

    public OpenApiBuilder(OpenAPIDesc desc) {
        this.desc = desc;
    }

    public OpenAPI build() {
        String version = Optional.ofNullable(desc.getVersion()).orElse("3.0.1");
        OpenAPI api = new OpenAPI().openapi(version);
        api.setInfo(buildInfo());
        if (desc.getSecurity() != null) {
            buildOAuthSchemes();
            buildJwtBearerScheme();
        }
        buildParameters();
        api.setComponents(components);
        return api;
    }

    private Info buildInfo() {
        Info info = new Info();
        if (desc.getInfo() != null) {
            info.setTitle(desc.getInfo().getTitle());
            info.setDescription(desc.getInfo().getDescription());
            info.setVersion(desc.getInfo().getVersion());
            info.setContact(desc.getInfo().getContact());
        }
        return info;
    }

    private void buildOAuthSchemes() {
        if (desc.getSecurity().getOauth2() == null) {
            return;
        }

        Scopes scopes = new Scopes();
        int flows = 0;
        OAuthFlows oAuthFlows = new OAuthFlows();

        OpenAPIDesc.OAuth2 oauth2 = desc.getSecurity().getOauth2();

        if (oauth2.isAuthorizationCodeFlow()) {
            flows++;
            OAuthFlow passwordFlow = new OAuthFlow();
            passwordFlow.setTokenUrl(oauth2.getUrl() + "/token");
            passwordFlow.setScopes(scopes);
            oAuthFlows.setPassword(passwordFlow);
        }

        if (flows > 0) {
            components.addSecuritySchemes(SecurityScheme.Type.OAUTH2.name().toUpperCase(),
                new SecurityScheme()
                    .description("OAuth2 OpenID Connect")
                    .type(SecurityScheme.Type.OAUTH2)
                    .flows(oAuthFlows)
            );
        }
    }

    private void buildJwtBearerScheme() {
        if (!desc.getSecurity().isJwtBearer()) {
            return;
        }
        components.addSecuritySchemes("JWT",
            new SecurityScheme()
                .description("JWT Beader Auth")
                .scheme("bearer")
                .bearerFormat("Bearer [token]")
                .type(SecurityScheme.Type.HTTP)
        );

    }

    private void buildParameters() {

        if (desc.getParameters()==null || desc.getParameters().isEmpty()) {
            return;
        }

        for(OpenAPIDesc.Parameter param : desc.getParameters()) {
            String ref = Optional.ofNullable(param.getRef()).orElse(param.getName());
            String name = Optional.ofNullable(param.getName()).orElse(param.getRef());

            Preconditions.checkArgument(TextUtil.isNotEmpty(name), "openapi parameter name or ref is required");
            Preconditions.checkArgument(TextUtil.isNotEmpty(param.getIn()), "openapi parameter.in is required");

            Parameter parameter = new Parameter();
            parameter.setIn(param.getIn().toUpperCase());
            parameter.setDescription(param.getDescription());
            parameter.setName(name);
            parameter.set$ref(ref);
            parameter.setAllowEmptyValue(param.isNullable());
            parameter.setRequired(!param.isNullable());

            Schema<String> schema = new Schema<>();
            schema.setType(param.getType());
            schema.setNullable(param.isNullable());

            if (CollectionUtil.isNotEmpty(param.getValues())) {
                schema.setEnum(param.getValues());
                schema.setDefault(param.getValue());
                parameter.setSchema(schema);
            }else if (TextUtil.isNotEmpty(param.getValue())) {
                schema.setDefault(param.getValue());
            }
            parameter.setSchema(schema);
            components.addParameters(ref, parameter);
        }

    }
}
