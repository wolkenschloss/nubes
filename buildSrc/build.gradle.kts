@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings

group = "family.haschka.wolkenschloss"

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    java
    `jvm-test-suite`
    groovy
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.4"
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

val fixtures: SourceSet by sourceSets.creating {
    java {
        setSrcDirs(testDir.dir(this.name).java)
    }

    kotlin {
        sourceSets[this@creating.name].apply {
            kotlin.setSrcDirs(testDir.dir(this@creating.name).kotlin)
        }
    }
}

val fixturesImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val fixturesRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

val fixturesApi: Configuration by configurations.getting {
    extendsFrom(configurations.api.get())
}

testing {
    suites {
        named("test", JvmTestSuite::class) {
            useJUnitJupiter(nubesLibs.versions.junit.get())
            sources {
                java {
                    setSrcDirs(listOf(testDir.unit.kotlin))
                }

                compileClasspath += fixtures.output
                runtimeClasspath += fixtures.output
            }
        }

        register("integration", JvmTestSuite::class) {
            useJUnitJupiter(nubesLibs.versions.junit.get())
            testType.set(TestSuiteType.INTEGRATION_TEST)

            sources {
                java {
                    setSrcDirs(listOf(testDir.dir(this@register.name).kotlin))
                }

                compileClasspath += fixtures.output
                runtimeClasspath += fixtures.output
            }

            dependencies {
                implementation(project)
                implementation(gradleKotlinDsl())
            }
        }

        register("functional", JvmTestSuite::class) {
            useJUnitJupiter(nubesLibs.versions.junit.get())
            testType.set(TestSuiteType.FUNCTIONAL_TEST)

            sources {
                java {
                    setSrcDirs(listOf(testDir.dir(this@register.name).kotlin))
                }

                compileClasspath += fixtures.output
                runtimeClasspath += fixtures.output
            }

            dependencies {
                implementation(project)
                implementation(gradleKotlinDsl())
            }
        }
    }
}

val testApi: Configuration by configurations.getting {
    extendsFrom(fixturesApi)
}

val integrationImplementation: Configuration by configurations.getting {
    extendsFrom(fixturesImplementation)
}

val integrationRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(fixturesRuntimeOnly)
}

val integrationApi: Configuration by configurations.getting {
    extendsFrom(fixturesApi)
}

val functionalImplementation: Configuration by configurations.getting {
    extendsFrom(fixturesImplementation)
}

val functionalRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(fixturesRuntimeOnly)
}

val functionalApi: Configuration by configurations.getting {
    extendsFrom(fixturesApi)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

gradlePlugin {

    testSourceSets(
        sourceSets.named("integration").get(),
        sourceSets.named("functional").get()
    )

    val namespace = "family.haschka.wolkenschloss"

    plugins {
        create("TestbedPlugin") {
            id = "$namespace.testbed"
            displayName = "Wolkenschloss Testbed Plugin"
            description = "Manages Testbed for Wolkenschloss"
            implementationClass = "family.haschka.wolkenschloss.gradle.testbed.TestbedPlugin"
        }

        create("CaPlugin") {
            id = "$namespace.ca"
            displayName = "Wolkenschloss CA Plugin"
            description = "Creates CA for Wolkenschloss"
            implementationClass = "family.haschka.wolkenschloss.gradle.ca.CaPlugin"
        }
    }
}

dependencies {
    implementation(nubesLibs.quarkus.plugin) {
        // This exclusion prevents the StaticLoggerBinder from being bound twice in the tests
        exclude(group = "org.jboss.slf4j", module = "slf4j-jboss-logmanager")
    }

    implementation(nubesLibs.gradle.node.plugin)
    implementation(nubesLibs.jib)
    api(nubesLibs.jsonpath)
    api(nubesLibs.bundles.bouncycastle)

    fixturesApi(nubesLibs.bundles.kotest)
}

tasks.withType<Test> {

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
    register("ci") {
        description = "Continuous Integration"
        group = VERIFICATION_GROUP
        dependsOn(
            "build",
            "integration",
            "functional"
        )
    }
}

val buildSrcPackagePrefix = "family.haschka.wolkenschloss"

idea {
    module {
        jdkName = "11"

        settings {
            packagePrefix["src/main/kotlin"] = buildSrcPackagePrefix
            packagePrefix["src/test/integration/kotlin"] = buildSrcPackagePrefix
            packagePrefix["src/test/functional/kotlin"] = buildSrcPackagePrefix
            packagePrefix["src/test/unit/kotlin"] = buildSrcPackagePrefix
            packagePrefix["src/test/fixtures/kotlin"] = buildSrcPackagePrefix
        }
    }
}
