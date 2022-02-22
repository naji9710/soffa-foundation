plugins {
    id("soffa.java8")
    id("soffa.springboot")
    id("soffa.qa.coverage.l2")
}

dependencies {
    implementation(project(":foundation-service"))
    implementation(project(":foundation-support-data"))
    testImplementation(project(":foundation-service-test"))
}
