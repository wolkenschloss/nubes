rootProject.name = "nubes"


enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")


pluginManagement {
        repositories {
                mavenLocal()
                mavenCentral()
                gradlePluginPortal()
        }
}

include (
        "services:cookbook:core",
        "services:cookbook:service",
        "services:cookbook:webapp",
        "testbed"
)

