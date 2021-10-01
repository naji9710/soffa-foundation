package io.soffa.foundation.data;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;

public class MockDataSource extends SimpleDriverDataSource {

    public MockDataSource() {
        super(new org.h2.Driver(), "jdbc:h2:mem:noop");
    }

}
