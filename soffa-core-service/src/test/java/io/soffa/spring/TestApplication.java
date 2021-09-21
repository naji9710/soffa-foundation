package io.soffa.spring;

import io.soffa.commons.jwt.JwtDecoder;
import io.soffa.commons.jwt.JwtJwksDecoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestApplication {


    @Bean
    public JwtDecoder createJwtDecoder(){
        return new JwtJwksDecoder("/security/jwks-dev.pub.json");
    }

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

}