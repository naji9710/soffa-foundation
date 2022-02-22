function fn(creds) {
    var helper = Java.type('io.soffa.foundation.test.KarateTestUtil');
    return helper.basicAuth(creds.username, creds.password);
}
