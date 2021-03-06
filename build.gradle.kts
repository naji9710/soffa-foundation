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
        classpath("io.soffa.gradle:soffa-gradle-plugin:2.2.0")
    }
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://plugins.gradle.org/m2/")
    }
}

apply(plugin = "soffa.sonatype-publish")

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "soffa.java8")

}
