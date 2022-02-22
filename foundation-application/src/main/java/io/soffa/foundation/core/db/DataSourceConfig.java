package io.soffa.foundation.core.db;

import lombok.Data;

@Data
public class DataSourceConfig {

    private String name;
    private String url;
    private String migration;
    private String tablesPrefix;

}
