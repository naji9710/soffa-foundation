package io.soffa.foundation.spring;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@ComponentScan("io.soffa.foundation.spring.config")
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class AutoConfigure {
}
