plugins {
    id("wolkenschloss.conventions.service")
    id("idea")
}

dependencies {
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-resteasy-reactive-jsonb")
    implementation("io.quarkus:quarkus-mongodb-panache")
    implementation("io.quarkus:quarkus-vertx")
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-web-client")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging")
    implementation("io.quarkus:quarkus-resteasy-reactive-qute")
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

// Treat custom source set as a test source in IntelliJ IDEA
// https://youtrack.jetbrains.com/issue/IDEA-151925#focus=Comments-27-5115263.0-0
idea {
    module {
        testSourceDirs = testSourceDirs.plus(sourceSets["integrationTest"].java.srcDirs)
        testResourceDirs = testResourceDirs.plus(sourceSets["integrationTest"].resources.srcDirs)
    }
}
