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