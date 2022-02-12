rootProject.name = "fixture-webapp"

dependencyResolutionManagement {
    versionCatalogs {

        // Kein blassen Schimmer, warum das funktioniert. Also lassen wir
        // das mal so stehen.
        // Siehe https://youtrack.jetbrains.com/issue/KTIJ-19370
        create("libs") {
            from(files(System.getProperty("project.catalog.directory")))
        }
    }
}