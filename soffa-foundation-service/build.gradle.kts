plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.springboot.library")
}


dependencies {
    api(project(":soffa-foundation-application"))
    api(project(":soffa-foundation-commons"))
    //compileOnly(project(":soffa-foundation-commons-pubsub"))

    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    runtimeOnly("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("net.logstash.logback:logstash-logback-encoder:7.0.1")
    
    api("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "com.zaxxer")
    }
    //implementation("org.springframework.boot:spring-boot-starter-amqp")
    api("org.springframework.cloud:spring-cloud-starter-vault-config")
    api("org.springframework.boot:spring-boot-starter-thymeleaf")
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    @Suppress("GradlePackageUpdate")
    implementation("com.zaxxer:HikariCP:4.0.3")

    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.33.0")
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.33.0")


    /*
    api("org.jobrunr:jobrunr:4.0.7") {
        exclude(group = "com.zaxxer")
        exclude(group = "com.h2database")
    }

     */
    api("org.jdbi:jdbi3-core:3.27.1") {
        exclude(group = "com.github.ben-manes.caffeine")
    }
    api("io.micrometer:micrometer-registry-prometheus")
    implementation("org.postgresql:postgresql:42.3.2")
    api("org.springdoc:springdoc-openapi-ui:1.6.5"){
        exclude(group = "io.github.classgraph")
    }
    implementation("org.springdoc:springdoc-openapi-security:1.6.5"){
        exclude(group = "io.github.classgraph")
    }
    implementation("io.github.classgraph:classgraph:4.8.138")
    testImplementation(project(":soffa-foundation-test"))
}

