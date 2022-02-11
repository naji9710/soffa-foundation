package io.soffa.foundation.spring;

import com.google.common.base.CaseFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringCaseTest {


    @Test
    public void testStringCase() {
        String output = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "helloOperation");
        assertEquals("hello_operation", output);
    }

}
