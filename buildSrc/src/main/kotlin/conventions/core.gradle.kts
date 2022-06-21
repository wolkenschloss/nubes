package family.haschka.wolkenschloss.conventions

plugins {
    id("family.haschka.wolkenschloss.conventions.java")
    id("java-library")
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")

dependencies {
    libs.findLibrary("junit-bom").ifPresent {
        testImplementation(platform(it))
    }

    libs.findLibrary("junit-jupiter").ifPresent {
        testImplementation(it)
    }
}

tasks {
    withType(Test::class.java) {
        useJUnitPlatform()
    }
}