package io.soffa.foundation.config;

import io.soffa.foundation.commons.CollectionUtil;
import io.soffa.foundation.web.OpenAPIDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppConfig {

    private boolean sysLogs = true;
    private DbConfig db;
    private OpenAPIDesc openapi;
    private SecurityConfig security;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasDataSources() {
        return db != null && CollectionUtil.isNotEmpty(db.getDatasources());
    }

}
