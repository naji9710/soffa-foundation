package ext.springboot;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableScheduling
@Configuration
@ComponentScan({"io.soffa.foundation.spring"})
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@AllArgsConstructor
public class FoundationAutoConfiguration {
}
