package io.soffa.foundation.core.application;

import io.soffa.foundation.commons.CollectionUtil;
import io.soffa.foundation.core.web.OpenAPIDesc;
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
