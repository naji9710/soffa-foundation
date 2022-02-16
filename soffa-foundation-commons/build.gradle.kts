plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.test.junit5")

}

dependencies {
    api(project(":soffa-foundation-api"))

    api("commons-beanutils:commons-beanutils:1.9.4")
    // api("io.github.openfeign:feign-core:11.8")
    api("joda-time:joda-time:2.10.13")
    api("com.github.michaelgantman:MgntUtils:1.6.0.1")
    api("com.joestelmach:natty:0.13")
    api("org.json:json:20211205")
    api("com.jayway.jsonpath:json-path:2.7.0")

    api("commons-io:commons-io:2.11.0")
    api("com.squareup.okhttp3:okhttp:4.9.3")
    api("com.konghq:unirest-java:3.13.6")
    api("com.google.guava:guava:31.0.1-jre")
    @Suppress("GradlePackageUpdate")
    implementation("commons-codec:commons-codec:1.15")


    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.1")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.1")

}

