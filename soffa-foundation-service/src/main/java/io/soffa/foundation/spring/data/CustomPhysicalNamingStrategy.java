package io.soffa.foundation.spring.data;

import io.soffa.foundation.commons.TextUtil;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CustomPhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    public static String tablePrefix;

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment env) {
        if (TextUtil.isEmpty(tablePrefix)) {
            return name;
        }
        String tableName = tablePrefix + name;
        return new Identifier(tableName, false);
    }


}
