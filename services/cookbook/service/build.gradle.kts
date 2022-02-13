import wolkenschloss.gradle.testbed.domain.DomainExtension

plugins {
    id("wolkenschloss.conventions.service")
    id("idea")
}

dependencies {
    implementation(libs.quarkus.container.docker)
    implementation(libs.quarkus.config.yaml)
    implementation(libs.quarkus.arc)
    implementation(libs.quarkus.resteasy.reactive.jsonb)
    implementation(libs.quarkus.mongodb.panache)
    implementation(libs.quarkus.vertx)
    implementation(libs.smallrye.mutiny.vertx.web.client)
    implementation(libs.quarkus.smallrye.reactive.messaging)
    implementation(libs.quarkus.resteasy.reactive.qute)
    implementation(libs.jsoup)

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

// Treat custom source set as a test source in IntelliJ IDEA
// https://youtrack.jetbrains.com/issue/IDEA-151925#focus=Comments-27-5115263.0-0
idea {
    module {
        testSourceDirs = testSourceDirs.plus(sourceSets["integrationTest"].java.srcDirs)
        testResourceDirs = testResourceDirs.plus(sourceSets["integrationTest"].resources.srcDirs)
    }
}
