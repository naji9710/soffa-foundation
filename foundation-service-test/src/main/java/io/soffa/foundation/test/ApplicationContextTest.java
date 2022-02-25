package io.soffa.foundation.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class ApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testContext(){
        assertNotNull(context);
    }

}
