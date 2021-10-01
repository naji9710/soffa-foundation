plugins {
    id("soffa.java8")
    id("soffa.springboot")
    id("soffa.qa.coverage.l2")
}

dependencies {
    implementation(project(":soffa-foundation-service"))
    testImplementation(project(":soffa-foundation-test"))
    runtimeOnly("com.h2database:h2:1.4.200")

}
