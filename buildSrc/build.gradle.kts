import org.gradle.plugins.ide.idea.model.IdeaModule
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    java
    id("idea")
//    kotlin("jvm") version "1.5.31"
}

val testDir = project.layout.projectDirectory.dir("src/test")

val Directory.java : Iterable<RegularFile>
    get() = listOf(this.file("java"))

val Directory.kotlin : Iterable<RegularFile>
    get() = listOf(this.file("kotlin"))

val Directory.resources : Iterable<RegularFile>
    get() = listOf(this.file("resources"))

val testing: SourceSet by sourceSets.creating {
    val name = this.name
    val testing = testDir.dir("testing")
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
    java.setSrcDirs(testing.java)
    resources.setSrcDirs(listOf(testing.resources))

    kotlin {
        sourceSets["testing"].apply {
            kotlin.setSrcDirs(listOf(testing.kotlin))
        }
    }
}

val testingImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val testingRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

val integration: SourceSet by sourceSets.creating {
    val name = this.name
    val base = testDir.dir(name)
    compileClasspath += sourceSets.main.get().output //+ testing.output
    runtimeClasspath += sourceSets.main.get().output //+ testing.output
    java.setSrcDirs(listOf(base.java))
    resources.setSrcDirs(listOf(base.resources))

    kotlin {
        sourceSets["integration"].apply {
            kotlin.setSrcDirs(listOf(base.kotlin))
        }
    }
}

val integrationImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val functional: SourceSet by sourceSets.creating {
    val name = this.name
    val base = testDir.dir(name)
    compileClasspath += sourceSets.main.get().output + testing.output
    runtimeClasspath += sourceSets.main.get().output + testing.output
    java.setSrcDirs(listOf(base.java))
    resources.setSrcDirs(listOf(base.resources))

    kotlin {
        sourceSets[name].apply {
            kotlin.setSrcDirs(listOf(base.kotlin))
        }
    }
}

val functionalImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val functionalRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

sourceSets.test {
    val name = this.name
    val base = testDir.dir("unit")
    compileClasspath += testing.output
    runtimeClasspath += testing.output
    java.setSrcDirs(listOf(base.java))
    resources.setSrcDirs(listOf(base.resources))

    kotlin {
        sourceSets[name].apply {
            kotlin.setSrcDirs(listOf(base.kotlin))
        }
    }
}

fun IdeaModule.registerSourceSet(sourceSet: SourceSet) {
    val module = this
    module.testSourceDirs = module.testSourceDirs.plus(sourceSet.java.srcDirs)
}

idea {
    module {
        listOf(sourceSets.test.get(), testing, integration, functional).forEach {
            registerSourceSet(it)
        }

        kotlin {
            sourceSets["integration"].apply {
            testSourceDirs = testSourceDirs.plus(this.kotlin.srcDirs)
            }
        }

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
    testSourceSets(testing, integration, functional)

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

    implementation(platform("com.github.docker-java:docker-java-bom:3.2.12"))
    implementation("com.github.docker-java:docker-java-core")
    implementation("com.github.docker-java:docker-java-transport-zerodep")

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

//    testingApi(gradleApi())
    testingImplementation("com.github.docker-java:docker-java-core:3.2.12")
    testingImplementation(gradleTestKit())
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

tasks {
    val integration by registering(Test::class) {
        description = "Runs integration tests."
        group = "verification"
        testClassesDirs = integration.output.classesDirs
        classpath = integration.runtimeClasspath
        shouldRunAfter("test")
    }

    val ft by registering(Test::class) {
        description = "Runs functional tests."
        group = "verification"
        testClassesDirs = functional.output.classesDirs
        classpath = functional.runtimeClasspath
        shouldRunAfter("it")
    }

    register("ci") {
        dependsOn("build", integration, "ft")
    }
}

