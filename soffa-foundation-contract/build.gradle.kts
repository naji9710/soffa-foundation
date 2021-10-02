plugins {
    id("soffa.project")
    id("soffa.maven-publish")
    id("soffa.test.junit5")
}

dependencies {
    api("io.github.openfeign:feign-core:11.6")
    api("javax.validation:validation-api:2.0.1.Final")
    api("javax.inject:javax.inject:1")
    api("com.fasterxml.jackson.core:jackson-annotations:2.12.5")
    api("javax.annotation:javax.annotation-api:1.3.2")
    api("io.swagger.core.v3:swagger-annotations:2.1.10")
    api("io.swagger.core.v3:swagger-models:2.1.10")
    api("org.apache.commons:commons-lang3:3.12.0")
}

