package ext.springboot;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableScheduling
@Configuration
@ComponentScan({"io.soffa.foundation.spring"})
//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class FoundationAutoConfiguration {
}
