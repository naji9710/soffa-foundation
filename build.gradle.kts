plugins {
    id("io.github.gradle-nexus.publish-plugin").version("1.1.0")
    idea
}


buildscript {
    repositories {
        mavenCentral()
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("io.soffa:soffa-gradle-plugin:2.0.2")
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
