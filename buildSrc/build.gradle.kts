import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.github.spullara.mustache.java:compiler:0.9.10")
        classpath("org.libvirt:libvirt:0.5.2")
        classpath("net.java.dev.jna:jna:5.8.0")
        classpath("com.jayway.jsonpath:json-path:2.6.0")
    }
}

plugins {
    groovy
    `groovy-gradle-plugin`
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
//    implementation gradleApi()
//    implementation("gradle.plugin.io.quarkus:quarkus-gradle-plugin:1.13.7.Final")
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")
    implementation("org.libvirt:libvirt:0.5.2")
    implementation("net.java.dev.jna:jna:5.8.0")
    implementation("com.google.cloud.tools:jib-core:0.19.0")

    implementation("com.jayway.jsonpath:json-path:2.6.0")

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0") {
        exclude(group = "org.codehaus.groovy")
    }


    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        // options for log level LIFECYCLE.
        // LIFECYCLE is gradle's default log level.
        // Starting gradle with options -q or -w only
        // the summarize is will be shown
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