package io.soffa.foundation.service.data;

import io.soffa.foundation.core.db.DataSourceConfig;
import lombok.Getter;

import javax.sql.DataSource;

@Getter
public class DatasourceInfo {

    private final String name;
    private DataSource dataSource;
    private final DataSourceConfig config;
    private boolean migrated;

    public DatasourceInfo(String name, DataSourceConfig config) {
        this.config = config;
        this.name = name.toLowerCase();
    }

    public DatasourceInfo(String name, DataSourceConfig config, DataSource dataSource) {
        this(name, config);
        this.dataSource = dataSource;
    }


    public void setMigrated(boolean value) {
        this.migrated = value;
    }
}
