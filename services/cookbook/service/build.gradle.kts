plugins {
    id("wolkenschloss.conventions.service")
    id("idea")
}


dependencies {
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jsonb")
    implementation("io.quarkus:quarkus-mongodb-panache")
    implementation("io.quarkus:quarkus-vertx")
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-web-client")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging")
    implementation("org.jsoup:jsoup:1.14.1")

    implementation(project(":services:cookbook:webapp"))

    testImplementation("org.awaitility:awaitility:4.1.0")

    integrationTestImplementation("org.testcontainers:testcontainers")
    integrationTestImplementation("org.testcontainers:mongodb")
    integrationTestImplementation("io.rest-assured:rest-assured:4.4.0")
    integrationTestImplementation("com.github.tomakehurst:wiremock-jre8:2.29.1")
    integrationTestImplementation("org.awaitility:awaitility:4.1.0")
}

// Treat custom source set as a test source in IntelliJ IDEA
// https://youtrack.jetbrains.com/issue/IDEA-151925#focus=Comments-27-5115263.0-0
idea {
    module {
        testSourceDirs = testSourceDirs.plus(sourceSets["integrationTest"].java.srcDirs)
        testResourceDirs = testResourceDirs.plus(sourceSets["integrationTest"].resources.srcDirs)
    }
}
