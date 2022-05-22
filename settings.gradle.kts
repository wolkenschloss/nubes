rootProject.name = "nubes"

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

