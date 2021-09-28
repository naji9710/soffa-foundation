package io.soffa.foundation.service.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class DbConfig {

    private Map<String,String> links;
    private boolean autoMigrate;
    private String tablePrefix;

}
