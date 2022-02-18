plugins {
    id("soffa.java8")
    id("soffa.maven-publish")
    id("soffa.springboot.library")
}


dependencies {
    api(project(":soffa-foundation-api"))
    api(project(":soffa-foundation-commons"))

    implementation("io.nats:jnats:2.13.2")
    implementation("com.github.fridujo:rabbitmq-mock:1.1.1")
    implementation("org.springframework.kafka:spring-kafka")
    //implementation("org.apache.kafka:kafka_2.13:3.1.0")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

}

