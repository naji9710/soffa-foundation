plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.test.junit5")
    id("soffa.springboot.library")

}

dependencies {
    api(project(":soffa-foundation-commons"))
    api("com.amazonaws:aws-java-sdk-s3:1.12.159")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.3")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.3")
    testImplementation(project(":soffa-foundation-service-test"))
}

