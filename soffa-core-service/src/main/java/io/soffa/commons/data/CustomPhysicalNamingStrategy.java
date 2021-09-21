package io.soffa.commons.data;

import io.soffa.commons.lang.TextUtil;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

public class CustomPhysicalNamingStrategy extends SpringPhysicalNamingStrategy {

    public static String tablePrefix;

    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment env) {
        if (TextUtil.isEmpty(tablePrefix)) {
            return name;
        }
        String tableName = tablePrefix + "_" + name;
        return new Identifier(tableName, false);
    }


}
