plugins {
    id("mycloud.service-conventions")
}

dependencies {
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-mongodb-panache")

    implementation(project(":services:cookbook:webapp"))

    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-junit5-mockito")

    integrationTestImplementation("org.testcontainers:testcontainers")
    integrationTestImplementation("org.testcontainers:junit-jupiter")
    integrationTestImplementation("org.testcontainers:mongodb")
    integrationTestImplementation("io.rest-assured:rest-assured")
}
