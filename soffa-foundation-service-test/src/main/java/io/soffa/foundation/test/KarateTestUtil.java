package io.soffa.foundation.test;

import io.soffa.foundation.context.AuthUtil;

public final class KarateTestUtil {

    private KarateTestUtil(){}

    public static String basicAuth(String username, String pasword) {
        return AuthUtil.createBasicAuthorization(username, pasword);
    }

}
