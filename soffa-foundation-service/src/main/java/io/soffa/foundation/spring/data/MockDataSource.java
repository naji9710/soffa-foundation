package io.soffa.foundation.spring.data;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;

public class MockDataSource extends SimpleDriverDataSource {

    public MockDataSource() {
        super(new org.h2.Driver(), "jdbc:h2:mem:noop;MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE");
    }

}
