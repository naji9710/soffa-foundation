package io.soffa.foundation.test;

import io.soffa.foundation.commons.http.HttpUtil;

public final class KarateTestUtil {

    private KarateTestUtil(){}

    public static String basicAuth(String username, String pasword) {
        return HttpUtil.createBasicAuthorization(username, pasword);
    }

}
