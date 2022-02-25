plugins {
    id("soffa.java8")
    id("soffa.springboot")
    id("soffa.qa.coverage.l6")
}

dependencies {
    implementation(project(":foundation-service"))
    implementation(project(":foundation-support-email"))
    testImplementation(project(":foundation-service-test"))
}
