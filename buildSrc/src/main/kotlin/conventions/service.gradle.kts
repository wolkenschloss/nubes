package family.haschka.wolkenschloss.conventions

plugins {
    id("family.haschka.wolkenschloss.conventions.java")
    id("java-library")
    id("io.quarkus")
}

tasks {
    withType(Test::class) {
        systemProperty("org.jboss.logging.provider", "slf4j")
        systemProperty("java.util.logging.SimpleFormatter.format", "[JUNIT] %3\$s %4\$s: %5\$s%6\$s%n")
        reports {
            junitXml.apply{
                required.set(true)
                outputLocation.set(layout.buildDirectory.dir("test-results/${this@withType.name}/junit-xml"))
            }
            html.apply {
                required.set(true)
                outputLocation.set(layout.buildDirectory.dir("test-results/${this@withType.name}/junit-html"))
            }
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