@file:Suppress("UnstableApiUsage")
rootProject.name = "plugins"

dependencyResolutionManagement {
    versionCatalogs {
        // Der Katalog darf nicht so heißen, wie der ursprüngliche Name
        // der Toml Datei, weil sonst dir Code Completion für IntelliJ
        // nicht funktioniert.
        // Siehe https://youtrack.jetbrains.com/issue/KTIJ-19370.
        //
        // Wenn du diesen Namen änderst, musst du das Verzeichnis
        // .gradle/7.4.2/dependencies-accessors löschen!
        create("nubesLibs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}