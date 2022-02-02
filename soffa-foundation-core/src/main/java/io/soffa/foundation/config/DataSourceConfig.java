package io.soffa.foundation.config;

import lombok.Data;

@Data
public class DataSourceConfig {

    private String name;
    private String url;
    // private boolean syslog;
    private String migration;
    private boolean automigrate = true;
    private String tablesPrefix;

}
