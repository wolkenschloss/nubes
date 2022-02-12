import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

group = "com.github.wolkenschloss.gradle"

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    java
    groovy
    id("idea")
}

val testDir = project.layout.projectDirectory.dir("src/test")

val Directory.java: Iterable<RegularFile>
    get() = listOf(this.file("java"))

val Directory.kotlin: Iterable<RegularFile>
    get() = listOf(this.file("kotlin"))

val Directory.resources: Iterable<RegularFile>
    get() = listOf(this.file("resources"))

val Directory.unit: Directory
    get() = this.dir("unit")

val empty = emptyList<RegularFile>()

val testing: SourceSet by sourceSets.creating {
    val testing = testDir.dir(this.name)
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
    java.setSrcDirs(testing.java)
    resources.setSrcDirs(testing.resources)

    kotlin {
        sourceSets[this@creating.name].apply {
            kotlin.setSrcDirs(testing.kotlin)
        }
    }

    groovy.setSrcDirs(empty)
}

val testingImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val testingRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

val integration: SourceSet by sourceSets.creating {
    val base = testDir.dir(name)

    compileClasspath += sourceSets.main.get().output
    compileClasspath += testing.output

    runtimeClasspath += sourceSets.main.get().output
    runtimeClasspath += testing.output

    java.setSrcDirs(base.java + base.kotlin)
    resources.setSrcDirs(base.resources)

    kotlin {
        sourceSets[this@creating.name].apply {
            kotlin.setSrcDirs(base.kotlin)
        }
    }

    groovy.setSrcDirs(empty)
}

val integrationImplementation: Configuration by configurations.getting {
    extendsFrom(testingImplementation)
}

val integrationRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(testingRuntimeOnly)

}

val functional: SourceSet by sourceSets.creating {
    val base = testDir.dir(name)
    compileClasspath += sourceSets.main.get().output + testing.output
    runtimeClasspath += sourceSets.main.get().output + testing.output
    java.setSrcDirs(base.java +  base.kotlin)
    resources.setSrcDirs(base.resources)

    kotlin {
        sourceSets[this@creating.name].apply {
            kotlin.setSrcDirs(base.kotlin)
        }
    }

    groovy.setSrcDirs(empty)
}

val functionalImplementation: Configuration by configurations.getting {
    extendsFrom(testingImplementation)
}

val functionalRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(testingRuntimeOnly)
}

sourceSets.test {
    compileClasspath += testing.compileClasspath
    runtimeClasspath += testing.runtimeClasspath
    java.setSrcDirs(testDir.unit.java + testDir.unit.kotlin)
    resources.setSrcDirs(testDir.unit.resources)

    kotlin {
        sourceSets[this@test.name].apply {
            kotlin.setSrcDirs(testDir.unit.kotlin)
        }
    }

    groovy.setSrcDirs(empty)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
}

gradlePlugin {

    testSourceSets(integration, functional)
    val namespace = "com.github.wolkenschloss"

    plugins {
        create("TestbedPlugin") {
            id = "$namespace.testbed"
            displayName = "Wolkenschloss Testbed Plugin"
            description = "Manages Testbed for Wolkenschloss"
            implementationClass = "wolkenschloss.gradle.testbed.TestbedPlugin"
        }

        create("DockerPlugin") {
            id = "$namespace.docker"
            displayName = "Wolkenschloss Docker Plugin"
            description = "Creates docker images and executes container"
            implementationClass = "wolkenschloss.gradle.docker.DockerPlugin"
        }

        create("CaPlugin") {
            id = "$namespace.ca"
            displayName = "Wolkenschloss CA Plugin"
            description = "Creates CA for Wolkenschloss"
            implementationClass = "wolkenschloss.gradle.ca.CaPlugin"
        }
    }
}

val quarkusPluginVersion: String by project
val quarkusPluginArtifactId: String by project




dependencies {
    implementation("io.quarkus:${quarkusPluginArtifactId}:${quarkusPluginVersion}") {
        // This exclusion prevents the StaticLoggerBinder from being bound twice in the tests
        exclude(group = "org.jboss.slf4j", module = "slf4j-jboss-logmanager")
    }
    implementation("com.github.node-gradle:gradle-node-plugin:3.1.1")
    implementation(libraries.libvirt)
    implementation(libraries.jna)
    implementation(libraries.jib)
    implementation(libraries.jsonpath)

    implementation(libraries.bundles.docker.java)


    // testing: basic test frameworks promoted to unit [test], integration and functional
    testingImplementation(libraries.bundles.junit)
    testingImplementation(libraries.bundles.kotest)

    // Allow integration tests to use kotlin dsl

//    testImplementation(kotlin("gradle-plugin"))

    integrationImplementation(gradleKotlinDsl())
}

tasks.withType<Test> {

    useJUnitPlatform()

    // run tests, when fixtures change
    val fixtures = project.layout.projectDirectory.dir("fixtures")
    inputs.files(fixtures)
        .withPropertyName("fixtures")
        .withPathSensitivity(PathSensitivity.RELATIVE)

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
    systemProperty("project.catalog.directory", project.rootDir.resolve("../gradle/libs.versions.toml").absolutePath)
}

tasks {
    val integration by registering(Test::class) {
//        doNotTrackState("mach das immer")
        description = "Runs integration tests."
        group = VERIFICATION_GROUP
        testClassesDirs = integration.output.classesDirs
        classpath = integration.runtimeClasspath
    }

    val functional by registering(Test::class) {
        description = "Runs functional tests."
        group = VERIFICATION_GROUP
        testClassesDirs = functional.output.classesDirs
        classpath = functional.runtimeClasspath

        shouldRunAfter(integration)
    }

    register("ci") {
        description = "Continuous Integration"
        group = VERIFICATION_GROUP
        dependsOn("build", integration, functional)
    }
}

idea {
    module {
        listOf(integration, functional).forEach {
            testSourceDirs = testSourceDirs.plus(it.java.srcDirs)
            testResourceDirs = testResourceDirs.plus(it.resources)
        }

        jdkName = "11"
    }
}