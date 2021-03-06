package io.soffa.foundation;

import io.soffa.foundation.commons.jwt.JwtDecoder;
import io.soffa.foundation.commons.jwt.JwtJwksDecoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Bean
    public JwtDecoder createJwtDecoder() {
        return new JwtJwksDecoder("/security/jwks-dev.pub.json");
    }

}
