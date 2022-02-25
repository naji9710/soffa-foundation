plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.test.junit5")

}

dependencies {
    api(project(":foundation-api"))
    api(project(":foundation-commons"))
    api("javax.inject:javax.inject:1")
    api("javax.transaction:javax.transaction-api:1.3")
    api("com.amazonaws:aws-java-sdk-s3:1.12.159")
    implementation("com.auth0:java-jwt:3.18.3")
    implementation("com.nimbusds:nimbus-jose-jwt:9.19")

    @Suppress("GradlePackageUpdate")
    api("com.github.ben-manes.caffeine:caffeine") {  // Don't use version 3, it's not compatible with Java8
        version {
            strictly("2.9.3")
        }
    }
    @Suppress("GradlePackageUpdate")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.3")

}

// api("com.jayway.jsonpath:json-path:2.7.0")
