plugins {
    id("soffa.java8")
    id("soffa.springboot")
    id("soffa.qa.coverage.l6")
}

dependencies {
    implementation(project(":soffa-foundation-service"))
    testImplementation(project(":soffa-foundation-service-test"))
}
