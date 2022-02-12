package io.soffa.foundation.test;

import io.soffa.foundation.service.state.DatabasePlane;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class DatabaseTest {

    @Autowired
    protected DatabasePlane dbPlane;

    @BeforeEach
    public void awaitDatabase() {
        dbPlane.await();
    }

}
