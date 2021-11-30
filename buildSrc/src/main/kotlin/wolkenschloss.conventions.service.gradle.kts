plugins {
    id("wolkenschloss.conventions.java")
    id("java-library")
    id("io.quarkus")
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

tasks {
    val integrationTest by registering(Test::class) {
        description = "Runs integration tests."
        group = "verification"
        dependsOn("assemble")

        systemProperty("build.output.directory", buildDir.absolutePath)
        systemProperty("org.jboss.logging.provider", "slf4j")
        systemProperty("java.util.logging.SimpleFormatter.format", "[JUNIT] %3\$s %4\$s: %5\$s%6\$s%n")

        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath

        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
    }

    named<Test>("test") {
        systemProperty("org.jboss.logging.provider", "slf4j")
        systemProperty("java.util.logging.SimpleFormatter.format", "[JUNIT] %3\$s %4\$s: %5\$s%6\$s%n")
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
    }
    check {
        dependsOn(integrationTest.get())
    }
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.rest-assured:rest-assured")

    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    integrationTestImplementation(platform("org.junit:junit-bom:5.8.1"))
    integrationTestImplementation("org.junit.jupiter:junit-jupiter-api")
    integrationTestRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    integrationTestImplementation("io.quarkus:quarkus-junit5")
    integrationTestImplementation("io.rest-assured:rest-assured")
}