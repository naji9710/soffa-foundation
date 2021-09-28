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
        classpath("io.soffa:soffa-gradle-plugin:2.0.5")
        classpath("io.github.gradle-nexus:publish-plugin:1.1.0")
    }
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://plugins.gradle.org/m2/")
    }
}

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "soffa.java8")
    apply(plugin = "soffa.maven-publish")
}
