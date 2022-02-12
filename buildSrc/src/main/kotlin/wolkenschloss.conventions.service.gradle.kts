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

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")

dependencies {
    libs.findLibrary("quarkus-bom").ifPresent { bom ->
        implementation(platform(bom))
    }

    libs.findBundle("quarkus-unit").ifPresent {
        testImplementation(it)
    }

    libs.findBundle("junit").ifPresent {
        testImplementation(it)
        integrationTestImplementation(it)
    }

    libs.findBundle("quarkus-integration").ifPresent {
        integrationTestImplementation(it)
    }
}