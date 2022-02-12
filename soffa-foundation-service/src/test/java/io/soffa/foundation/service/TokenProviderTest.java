package io.soffa.foundation.service;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.model.Authentication;
import io.soffa.foundation.tokens.Token;
import io.soffa.foundation.tokens.TokenConfig;
import io.soffa.foundation.tokens.TokenProvider;
import io.soffa.foundation.tokens.TokenType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TokenProviderTest {

    @Test
    public void testJwtEncoder() {
        TokenConfig config = new TokenConfig("test", "yujRAkZLDBW*Xaw3");
        TokenProvider tokens = new DefaultTokenProvider(config);

        Token token = tokens.create(TokenType.JWT, "Foundation", ImmutableMap.of("email", "foundation@soffa.io"));
        assertNotNull(token.getValue());
        assertEquals("Foundation", token.getSubject());

        Authentication auth = tokens.decode(token.getValue());
        assertNotNull(auth);
    }

}
