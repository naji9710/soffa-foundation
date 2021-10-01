package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FoundationTestApp {

    public static void main(String[] args) {
        SpringApplication.run(FoundationTestApp.class, args).getEnvironment();
    }

}
