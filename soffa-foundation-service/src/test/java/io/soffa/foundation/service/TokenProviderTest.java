package io.soffa.foundation.service;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.application.model.Authentication;
import io.soffa.foundation.application.model.Token;
import io.soffa.foundation.application.model.TokenType;
import io.soffa.foundation.application.security.DefaultTokenProvider;
import io.soffa.foundation.application.security.TokenProvider;
import io.soffa.foundation.application.security.model.TokensConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TokenProviderTest {

    @Test
    public void testJwtEncoder() {
        TokensConfig config = new TokensConfig("test", "yujRAkZLDBW*Xaw3");
        TokenProvider tokens = new DefaultTokenProvider(config);

        Token token = tokens.create(TokenType.JWT, "Foundation", ImmutableMap.of("email", "foundation@soffa.io"));
        assertNotNull(token.getValue());
        assertEquals("Foundation", token.getSubject());

        Authentication auth = tokens.decode(token.getValue());
        assertNotNull(auth);
    }

}
