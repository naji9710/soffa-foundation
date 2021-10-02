package io.soffa.foundation;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.jwt.JwtUtil;
import io.soffa.foundation.test.HttpExpect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EndpointTest {

    private final HttpExpect test;

    public EndpointTest(@Autowired MockMvc mvc) {
        test = new HttpExpect(mvc);
    }

    @Test
    public void testPublicEndpoint() {
        test.get("/public").expect().isOK();
    }

    @Test
    public void testSecureEndpoint() {
        test.get("/secure").expect().isUnauthorized();

        String jwt = JwtUtil.fromJwks(
                EndpointTest.class.getResourceAsStream("/security/jwks-dev.priv.json"),
                "application",
                "user",
            ImmutableMap.of(
                "application", "AppName",
                "tenant", "T1"
            )
        );
        test.get("/secure").bearerAuth(jwt).expect().isOK();

        test.get("/secure/full")
                .bearerAuth(jwt)
                .header("X-ApplicationName", "TEST")
                .expect().isOK();

        test.get("/secure/full")
                .bearerAuth(jwt)
                .header("X-TenantId", "BF")
                .expect().isOK();

        test.get("/secure/full")
                .bearerAuth(jwt)
                .header("X-ApplicationName", "App")
                .header("X-TenantId", "TX01")
                .expect().isOK();
    }

}
