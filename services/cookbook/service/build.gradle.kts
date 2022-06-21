import family.haschka.wolkenschloss.gradle.testbed.domain.DomainExtension
import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings

// see https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("family.haschka.wolkenschloss.conventions.service")

    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.noarg)
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.bundles.quarkus.deployment)
    implementation(libs.quarkus.config.yaml)
    implementation(libs.quarkus.arc)
    implementation(libs.quarkus.resteasy.reactive.jsonb)
    implementation(libs.quarkus.mongodb.panache)
    implementation(libs.quarkus.vertx)
    implementation(libs.smallrye.mutiny.vertx.web.client)
    implementation(libs.quarkus.smallrye.reactive.messaging)
    implementation(libs.quarkus.resteasy.reactive.qute)
    implementation(libs.jsoup)

    implementation(projects.services.cookbook.core)
    implementation(projects.services.cookbook.webapp)

    testImplementation(libs.awaitility)

    implementation(platform(libs.testcontainers))

    integrationTestImplementation("org.testcontainers:mongodb")
    integrationTestImplementation("org.testcontainers:mockserver")
    integrationTestImplementation("org.testcontainers:junit-jupiter")
    integrationTestImplementation(libs.mockserver)
    integrationTestImplementation(libs.awaitility)
}

noArg {
    annotations(
        "family.haschka.wolkenschloss.cookbook.Value",
        "family.haschka.wolkenschloss.cookbook.Aggregate",
    )
}

allOpen {
    preset("quarkus")
}

tasks {
    quarkusBuild {
        inputs.property("domain-suffix", System.getProperty(DomainExtension.DOMAIN_SUFFIX_PROPERTY))
            .optional(false)
        System.getProperty("quarkus.profile")?.also {
            inputs.property("profile", it)
        }
    }
}

group = "family.haschka.nubes.cookbook"
val buildSrcPackagePrefix = "family.haschka.wolkenschloss.cookbook"

idea {
    module {
        settings {
            packagePrefix["src/main/kotlin"] = buildSrcPackagePrefix
            packagePrefix["src/test/kotlin"] = buildSrcPackagePrefix
            packagePrefix["src/integrationTest/kotlin"] = buildSrcPackagePrefix
        }
    }
}