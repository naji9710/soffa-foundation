package io.soffa.foundation.application;

import io.soffa.foundation.application.context.DefaultRequestContext;
import io.soffa.foundation.application.security.model.SecurityConfig;
import io.soffa.foundation.commons.CollectionUtil;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.exposition.openapi.OpenAPIDesc;
import io.soffa.foundation.infrastructure.db.DbConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppConfig {

    private boolean sysLogs = true;
    private String name;
    private String version;
    private DbConfig db;
    private OpenAPIDesc openapi;
    private SecurityConfig security;

    private boolean configured;

    public AppConfig(String name) {
        this.name = name;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasDataSources() {
        return db != null && CollectionUtil.isNotEmpty(db.getDatasources());
    }

    public void configure() {
        if (configured) {
            return;
        }
        if (db != null && TextUtil.isNotEmpty(db.getTablesPrefix())) {
            String value = TextUtil.trimToEmpty(db.getTablesPrefix())
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+$", "_").trim();

            if (!value.endsWith("_")) {
                value += "_";
            }
            db.setTablesPrefix(value);
        }
        DefaultRequestContext.setServiceName(name);
        configured = true;
    }

}
