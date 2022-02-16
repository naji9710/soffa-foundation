package io.soffa.foundation.service;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.model.Authentication;
import io.soffa.foundation.security.DefaultTokenProvider;
import io.soffa.foundation.security.model.Token;
import io.soffa.foundation.security.model.TokensConfig;
import io.soffa.foundation.security.TokenProvider;
import io.soffa.foundation.security.model.TokenType;
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
