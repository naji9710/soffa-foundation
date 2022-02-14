package io.soffa.foundation.context;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class AuthUtil {

    private AuthUtil() {
    }

    public static String createBasicAuthorization(String username, String password) {
        final String pair = username + ":" + password;
        final String encoded = Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

}
