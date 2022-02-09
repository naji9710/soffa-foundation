package io.soffa.foundation.core.application;

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
