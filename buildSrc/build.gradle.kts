import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    groovy
    `kotlin-dsl`
    `java-gradle-plugin`
    java
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "wolkenschloss.testbed"
            implementationClass = "wolkenschloss.TestbedPlugin"
        }
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("io.quarkus:gradle-application-plugin:2.3.0.Final")
    implementation("com.github.node-gradle:gradle-node-plugin:3.1.0")
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")
    implementation("org.libvirt:libvirt:0.5.2")
    implementation("net.java.dev.jna:jna:5.8.0")
    implementation("com.google.cloud.tools:jib-core:0.19.0")

    implementation("com.jayway.jsonpath:json-path:2.6.0")

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0") {
        exclude(group = "org.codehaus.groovy")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    enabled = true
    testLogging {
        // options for log level LIFECYCLE.
        // LIFECYCLE is gradle's default log level.
        // Starting gradle with options -q or -w only
        // the summary is shown
        exceptionFormat = TestExceptionFormat.SHORT

        info {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}