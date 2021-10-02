plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.springboot.library")
}

dependencies {
    api(project(":soffa-foundation-contract"))
    api("org.springframework.boot:spring-boot-starter-web"){
        exclude(module="spring-boot-starter-tomcat")
    }
    runtimeOnly("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth:3.0.3")
    implementation("net.logstash.logback:logstash-logback-encoder:6.6")

    api("org.springframework.boot:spring-boot-starter-data-jpa")
    //implementation("org.springframework.amqp:spring-rabbit")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    //implementation("org.springframework.vault:spring-vault-core:2.3.2")
    api("org.springframework.cloud:spring-cloud-starter-vault-config")

    api("org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.2.Final")

    api("com.google.guava:guava:30.1.1-jre")

    // api("io.github.resilience4j:resilience4j-all:1.7.1")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.5")
    api("com.jayway.jsonpath:json-path:2.6.0")
    // api("com.github.ben-manes.caffeine:caffeine:3.0.4")
    api("javax.transaction:javax.transaction-api:1.3")
    api("org.hibernate.validator:hibernate-validator-annotation-processor:7.0.1.Final")
    api("org.hibernate.validator:hibernate-validator:7.0.1.Final")

    api("com.github.fridujo:rabbitmq-mock:1.1.1")
    api("org.springframework.boot:spring-boot-starter-thymeleaf")

    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
    implementation("com.h2database:h2:1.4.200")
    implementation("org.mockito:mockito-core:3.12.4")
    implementation("io.micrometer:micrometer-registry-prometheus")
    api("org.springdoc:springdoc-openapi-ui:1.5.10")
    implementation("org.springdoc:springdoc-openapi-security:1.5.10")
    // api("com.github.slugify:slugify:2.5")
    api("joda-time:joda-time:2.10.10")
    implementation("com.joestelmach:natty:0.13")
    implementation("com.aventrix.jnanoid:jnanoid:2.0.0")

    api("commons-io:commons-io:2.8.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.5")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.5")
    // implementation("commons-beanutils:commons-beanutils:1.9.4")
    implementation("org.json:json:20210307")
    implementation("com.nimbusds:nimbus-jose-jwt:9.14")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.konghq:unirest-java:3.13.0")
    implementation("com.github.michaelgantman:MgntUtils:1.5.1.2")
    api("org.jdbi:jdbi3-core:3.22.0") {
        exclude(group = "com.github.ben-manes.caffeine")
    }
    api("org.liquibase:liquibase-core:4.4.3")
    // testImplementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation(project(":soffa-foundation-test"))
    // testImplementation("org.springframework.boot:spring-boot-starter-amqp")
    // testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // testImplementation("org.springframework.cloud:spring-cloud-starter-vault-config")
}

