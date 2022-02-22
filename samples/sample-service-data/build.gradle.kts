plugins {
    id("soffa.java8")
    id("soffa.springboot")
    id("soffa.qa.coverage.l2")
}

dependencies {
    implementation(project(":soffa-foundation-service"))
    implementation(project(":soffa-foundation-support-data"))
    testImplementation(project(":soffa-foundation-service-test"))
}
