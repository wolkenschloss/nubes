

buildscript {
    val quarkusPluginVersion: String by project
    val quarkusPluginArtifactId: String by project

    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("io.quarkus:${quarkusPluginArtifactId}:${quarkusPluginVersion}")
    }
}

allprojects {
    group = "family.haschka.nubes"
    version = "v0.0.1-alpha"
}
