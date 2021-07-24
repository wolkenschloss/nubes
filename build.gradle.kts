buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("io.quarkus:io.quarkus.gradle.plugin:2.1.0.Final")
    }
}

allprojects {
    group = "family.haschka.nubes"
    version = "0.0.1-SNAPSHOT"
}
