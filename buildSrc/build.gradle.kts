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
    id("idea")
    id("org.unbroken-dome.test-sets") version "4.0.0"
}

testSets {
    @Suppress("UNUSED_VARIABLE") val functionalTest by creating
}

idea {
    module {
//        val functionalTest by testSets
//        testSourceDirs.addAll(functionalTest.sourceSet.allSource)
//        testResourceDirs.addAll(functionalTest.sourceSet.resources.srcDirs)
//
//        val unitTest by testSets
//        testSourceDirs.addAll(unitTest.sourceSet.allSource)
//        testResourceDirs.addAll(unitTest.sourceSet.resources.srcDirs)
    }
}

val JAVA_VERSION = JavaLanguageVersion.of(11)

java {
    toolchain {
        languageVersion.set(JAVA_VERSION)
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JAVA_VERSION)
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
}

gradlePlugin {
//    testSourceSets.add(project.sourceSets["functionalTest"])
    plugins {
        create("simplePlugin") {
            id = "wolkenschloss.testbed"
            implementationClass = "wolkenschloss.TestbedPlugin"
        }
        create("DockerPlugin") {
            id = "wolkenschloss.gradle.docker"
            implementationClass = "wolkenschloss.gradle.docker.DockerPlugin"
        }
    }
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
}

val quarkusPluginVersion: String by project
val quarkusPluginArtifactId: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    implementation("io.quarkus:${quarkusPluginArtifactId}:${quarkusPluginVersion}") {
        // This exclusion prevents the StaticLoggerBinder from being bound twice in the tests
        exclude(group = "org.jboss.slf4j", module = "slf4j-jboss-logmanager")
    }
    implementation("com.github.node-gradle:gradle-node-plugin:3.1.1")
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")
    implementation("org.libvirt:libvirt:0.5.2")
    implementation("net.java.dev.jna:jna:5.8.0")
    implementation("com.google.cloud.tools:jib-core:0.19.0")
    implementation("com.jayway.jsonpath:json-path:2.6.0")

    implementation("com.github.docker-java:docker-java-core:3.2.12")
    implementation("com.github.docker-java:docker-java-transport-zerodep:3.2.12")

    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // Waiting for Gradle update to use kotlin version 1.6.
    // Then the current version of kotest can also be used.
    // https://github.com/kotest/kotest/issues/2666
    testImplementation(platform("io.kotest:kotest-bom:4.6.3"))
    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("io.kotest:kotest-runner-junit5-jvm")
    testImplementation("io.kotest:kotest-assertions-core")
}

tasks.withType<Test> {

    // trigger build, if fixture changes
    // fixture noch nicht mit dabei. Test sollte das
    // verzeichnis nach tmp kopieren. node_modules
    // sollten nicht von gradle überwacht werden.
    val fixtures = project.layout.projectDirectory.dir("fixtures")
    inputs.files(fixtures)
        .withPropertyName("fixtures")
        .withPathSensitivity(PathSensitivity.RELATIVE)

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

    systemProperty("project.fixture.directory", fixtures.asFile.absolutePath)
}

