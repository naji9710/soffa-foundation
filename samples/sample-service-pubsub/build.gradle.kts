plugins {
    id("soffa.java8")
    id("soffa.springboot")
    id("soffa.qa.coverage.l1")
}

dependencies {
    implementation(project(":foundation-service"))
    implementation(project(":foundation-support-pubsub"))
    testImplementation(project(":foundation-service-test"))
}
