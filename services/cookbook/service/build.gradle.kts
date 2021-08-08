plugins {
    id("wolkenschloss.conventions.service")
}

dependencies {
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-resteasy-jsonb")
    implementation("io.quarkus:quarkus-mongodb-panache")
    implementation("org.jsoup:jsoup:1.14.1")

    implementation(project(":services:cookbook:webapp"))

    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-junit5-mockito")

    integrationTestImplementation("org.testcontainers:testcontainers")
    integrationTestImplementation("org.testcontainers:junit-jupiter")
    integrationTestImplementation("org.testcontainers:mongodb")
    integrationTestImplementation("io.rest-assured:rest-assured:4.4.0")
    integrationTestImplementation("com.github.tomakehurst:wiremock-jre8:2.29.1")
    integrationTestImplementation("org.awaitility:awaitility:4.1.0")
}
