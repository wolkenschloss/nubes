rootProject.name = "nubes"

pluginManagement {
        val quarkusPluginVersion: String by settings
        val quarkusPluginArtifactId: String by settings

        repositories {
                mavenLocal()
                mavenCentral()
        }
}
include (
        "services:cookbook:core",
        "services:cookbook:service",
        "services:cookbook:webapp",
        "testbed",
//        "ca",
)

