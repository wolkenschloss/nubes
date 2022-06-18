import family.haschka.wolkenschloss.gradle.testbed.domain.DomainExtension
import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings

plugins {
    id("family.haschka.wolkenschloss.conventions.service")

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.allopen") version "1.6.21"
    kotlin("plugin.noarg") version "1.6.21"
//    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.21"
//    id("org.jetbrains.kotlin.plugin.noarg") version "1.6.21"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.4"
}

noArg {
    annotations(
        "family.haschka.wolkenschloss.cookbook.Entity",
        "io.quarkus.mongodb.panache.common.MongoEntity")
    invokeInitializers = false
}

allOpen {
    annotation("family.haschka.wolkenschloss.cookbook.Entity")
}

dependencies {
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

tasks {
    quarkusBuild {
        inputs.property("domain-suffix", System.getProperty(DomainExtension.DOMAIN_SUFFIX_PROPERTY))
            .optional(false)
        System.getProperty("quarkus.profile")?.also {
            inputs.property("profile", it)
        }
    }
}

val buildSrcPackagePrefix = "family.haschka.wolkenschloss.cookbook"

idea {
    module {
        settings {
            packagePrefix["src/main/kotlin"] = buildSrcPackagePrefix
            packagePrefix["src/test/kotlin"] = buildSrcPackagePrefix
        }
    }
}