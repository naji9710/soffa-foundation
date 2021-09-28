plugins {
    id("soffa.java8")
    id("soffa.springboot.library")
}

dependencies {
    api("org.springframework:spring-test")
    api("org.springframework.boot:spring-boot-starter-test")
    api("org.springframework.boot:spring-boot-starter-web")
    implementation("com.h2database:h2:1.4.200")
}

