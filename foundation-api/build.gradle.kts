plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.test.junit5")

}

dependencies {
    api("javax.validation:validation-api:2.0.1.Final")
    api("org.checkerframework:checker-qual:3.21.2")
    api("org.checkerframework:checker:3.21.1")
    api("org.checkerframework:jdk8:3.3.0")
    api("javax.annotation:javax.annotation-api:1.3.2")
    api("io.swagger.core.v3:swagger-annotations:2.1.13")
    api("io.swagger.core.v3:swagger-models:2.1.13")
    api("javax.ws.rs:javax.ws.rs-api:2.1.1")
    api("com.fasterxml.jackson.core:jackson-annotations:2.13.1")

}

