package io.soffa.foundation.service.data;

import io.soffa.foundation.application.AppConfig;
import io.soffa.foundation.infrastructure.db.DB;
import io.soffa.foundation.infrastructure.db.TenantsLoader;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Configuration
public class DBConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TenantsLoader createDefaultTenantsLoader() {
        return TenantsLoader.NOOP;
    }

    @Bean
    public DB createDB(AppConfig appConfig,
                       ApplicationContext context,
                       @Value("${spring.application.name}") String applicationName) {

        appConfig.configure();
        return new DBImpl(context, appConfig.getDb(), applicationName);
    }

    @Bean
    @Primary
    public DataSource createDatasource(DB db) {
        return (DataSource) db;
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    public Jdbi jdbi(DB datasource) {
        return Jdbi.create(new TransactionAwareDataSourceProxy((DataSource) datasource))
            //.installPlugin(new PostgresPlugin())
            .installPlugin(new SqlObjectPlugin());
    }


}
