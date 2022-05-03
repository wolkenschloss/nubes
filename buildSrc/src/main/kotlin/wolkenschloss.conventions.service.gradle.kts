plugins {
    id("wolkenschloss.conventions.java")
    id("java-library")
    id("io.quarkus")
}

tasks {
    named<Test>("test") {
        systemProperty("org.jboss.logging.provider", "slf4j")
        systemProperty("java.util.logging.SimpleFormatter.format", "[JUNIT] %3\$s %4\$s: %5\$s%6\$s%n")
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
    }
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")

dependencies {
    libs.findLibrary("quarkus-bom").ifPresent { bom ->
        implementation(enforcedPlatform(bom))
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