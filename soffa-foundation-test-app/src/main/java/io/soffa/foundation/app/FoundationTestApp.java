package io.soffa.foundation.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"io.soffa.foundation"})
@EnableJpaRepositories(basePackages = {"io.soffa.foundation"})
public class FoundationTestApp {

    public static void main(String[] args) {
        SpringApplication.run(FoundationTestApp.class, args).getEnvironment();
    }

}
