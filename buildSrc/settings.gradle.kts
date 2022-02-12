enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "plugins"

dependencyResolutionManagement {
    versionCatalogs {
        // Der Katalog darf nicht so heißen, wie der ursprüngliche Name
        // der Toml Datei, weil sonst dir Code Completion für IntelliJ
        // nicht funktioniert.
        // Siehe https://youtrack.jetbrains.com/issue/KTIJ-19370
        create("libraries") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}