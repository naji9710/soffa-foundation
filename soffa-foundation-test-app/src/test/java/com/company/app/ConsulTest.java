package com.company.app;

import com.pszymczyk.consul.ConsulProcess;
import com.pszymczyk.consul.ConsulStarterBuilder;
import io.soffa.foundation.commons.http.HttpClient;
import io.soffa.foundation.test.HttpExpect;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "app.consul.discovery=true")
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ConsulTest {

    private static ConsulProcess consul = null;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setup() {
        consul = ConsulStarterBuilder.consulStarter().buildAndStart();
        System.setProperty("spring.cloud.consul.enabled", "true");
        System.setProperty("app.consul.host", "localhost");
        System.setProperty("app.consul.port", String.valueOf(consul.getHttpPort()));
    }

    @AfterAll
    public static void teardown() {
        consul.close();
    }

    @Test
    public void testActuator() {
        HttpExpect test = new HttpExpect(mvc);
        test.get("/actuator/health")
            .header("Access-Control-Request-Method", "GET")
            .header("Origin", "https://www.someurl.com")
            .expect().isOK().json("$.status", "UP");

        String consulTestEndpoint = "http://localhost:" + consul.getHttpPort() + "/v1/agent/self";
        assertTrue(HttpClient.getInstance().get(consulTestEndpoint).is2xxSuccessful());

    }

}
