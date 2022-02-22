plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.springboot.library")
}
dependencies {
    api(project(":foundation-commons"))
    api("com.amazonaws:aws-java-sdk-s3:1.12.159"){
        exclude(group="com.github.ben-manes.caffeine")
    }
    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3") // Don't use version 3, it's not compatible with Java8
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.3")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.3")
    testImplementation(project(":foundation-service-test"))
}

