buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.soffa:soffa-gradle-plugin:2.0.2")
    }
}

plugins {
    idea
}

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "soffa.java8")
    apply(plugin = "soffa.maven-publish")
}
