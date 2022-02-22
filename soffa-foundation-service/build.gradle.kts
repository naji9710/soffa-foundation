plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.springboot.library")
}


dependencies {
    api(project(":soffa-foundation-application"))
    api(project(":soffa-foundation-commons"))

    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    runtimeOnly("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("net.logstash.logback:logstash-logback-encoder:7.0.1")
    implementation("org.springframework.cloud:spring-cloud-starter-vault-config")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.postgresql:postgresql:42.3.2")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.6"){
        exclude(group = "io.github.classgraph")
    }
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.6")
    implementation("org.springdoc:springdoc-openapi-security:1.6.6"){
        exclude(group = "io.github.classgraph")
    }
    implementation("io.github.classgraph:classgraph:4.8.139")

    testImplementation(project(":soffa-foundation-service-test"))
}


