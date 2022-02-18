function fn() {
    karate.configure('connectTimeout', 5000);
    karate.configure('readTimeout', 5000);
    var config = {
        baseUrl : karate.properties['baseUrl']
    };
    return config;
}
