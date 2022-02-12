plugins {
    id("wolkenschloss.conventions.java")
    id("java-library")
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")

dependencies {
    libs.findBundle("junit").ifPresent {
        testImplementation(it)
    }
}

tasks {
    withType(Test::class.java) {
        useJUnitPlatform()
    }
}