import family.haschka.wolkenschloss.gradle.testbed.domain.DomainExtension

plugins {
    id("family.haschka.wolkenschloss.conventions.service")
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

    implementation(project(":services:cookbook:core"))
    implementation(project(":services:cookbook:webapp"))

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
