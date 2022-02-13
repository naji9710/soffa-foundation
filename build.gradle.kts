plugins {
    idea
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("io.soffa.gradle:soffa-gradle-plugin:2.2.4")
    }
}


apply(plugin = "soffa.sonatype-legacy-publish")

allprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "soffa.java8")

}
