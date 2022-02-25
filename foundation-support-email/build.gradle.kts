plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.test.junit5")

}

dependencies {
    api(project(":foundation-commons"))
    implementation("org.apache.commons:commons-email:1.5")
    implementation("com.sendgrid:sendgrid-java:4.8.3")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.3")

}

