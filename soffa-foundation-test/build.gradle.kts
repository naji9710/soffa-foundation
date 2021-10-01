plugins {
    id("soffa.java8")
    id("soffa.springboot.library")
    id("soffa.maven-publish")
}

dependencies {
    // api("org.springframework:spring-test")
    api("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google")

    }
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    api("com.h2database:h2:1.4.200")
    api("com.google.guava:guava:30.1.1-jre")
    api("commons-io:commons-io:2.8.0")
    api("org.awaitility:awaitility:4.1.0")
    implementation("org.mock-server:mockserver-netty:5.11.2")

    testImplementation("org.springframework.boot:spring-boot-starter-web")
}

