buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("io.quarkus:io.quarkus.gradle.plugin:1.13.7.Final")
    }
}

allprojects {
    group = "family.haschka.nubes"
    version = "0.0.1-SNAPSHOT"
}
