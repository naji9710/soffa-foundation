plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.springboot.library")
}


dependencies {
    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:latest.release"))
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
}
