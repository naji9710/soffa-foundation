plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.springboot.library")
}


dependencies {
    api(project(":soffa-foundation-application"))

    api("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "com.zaxxer")
    }
    implementation("org.postgresql:postgresql:42.3.2")
    @Suppress("GradlePackageUpdate")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.33.0")
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.33.0")
    api("org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.2.Final")
    implementation("org.jdbi:jdbi3-core:3.27.2") {
        exclude(group = "com.github.ben-manes.caffeine")
    }
    implementation("org.liquibase:liquibase-core:4.7.1")
    implementation("org.jdbi:jdbi3-postgres:3.27.2")
    implementation("org.jdbi:jdbi3-sqlobject:3.27.2")
}


/*
api("org.jobrunr:jobrunr:4.0.7") {
    exclude(group = "com.zaxxer")
    exclude(group = "com.h2database")
}

 */

