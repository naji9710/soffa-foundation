package io.soffa.foundation.config;

import lombok.Data;

import java.util.Map;

@Data
public class DbConfig {

    private String tablesPrefix;
    private String tenantListQuery;
    private Map<String,DataSourceConfig> datasources;

}
