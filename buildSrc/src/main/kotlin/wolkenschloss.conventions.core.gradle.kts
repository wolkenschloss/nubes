plugins {
    id("wolkenschloss.conventions.java")
    id("java-library")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    withType(Test::class.java) {
        useJUnitPlatform()
    }
}