import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.npm.task.NpmInstallTask

plugins {
    id("mycloud.java-conventions")
    id("com.github.node-gradle.node") version "3.1.0"
}

// Dieser Hack erlaubt es, im Quarkus Development Mode das
// Frontend als eingebettete Resource zu starten:
//
// ./gradlew :services:cookbook:service:quarkusDev
//
// Startet das Backend unter 0.0.0.0:8181. Das Frontend
// ist dann über http://0.0.0.0:8181/index.html erreichbar.
//
// Damit hat das Frontend dieselbe Adresse, wie das Backend.
// CORS muss nicht konfiguriert werden. Allerdings werden
// Änderungen, die am Quelltext des Frontends vorgenommen
// werden nicht sofort wirksam. Dazu muss der Quarkus
// Entwicklungsserver erste neu gebaut werden. Im
// Entwicklungsmodus berücksichtigt Quarkus die Jars der
// abhängigen Projekte nicht, sondern legt die Ausgabe des
// Build Verzeichnisses direkt in den Classpath. Mit diesem
// Hack wird das Build Ergebnis des Frontendprojekts auf den
// Classpath kopiert.
//
// Die Alternative besteht darin, das Frontend mit
// npm run serve zu starten und die Anfragen des Frontends
// an das Backend durch einen Proxy zu leiten, der alle
// Anfrage von 0.0.0.0:8080 auf 0.0.0.0:8181 umleitet.
// Bei dieser Lösung gibt es für jeden Microservice einen
// Port für das Backend und einen Port für das Frontend.
// Die Konfiguration kann auf lange Sicht umständlich sein.
// Ferner ist in der Entwicklung immer darauf zu achten,
// das Frontend und Backend gestartet werden.

// quarkusDev funktioniert nicht mit diesem Verzeichnis. Muss in classes/java/main liegen.
// def destination = "$buildDir/generated-resources/META-INF/resources/webapp"
var destination = "build/classes/java/main/META-INF/resources"
sourceSets {
    main {
        output.dir(destination, "builtBy" to listOf("generateVueApp"))
    }
}

node {
    // Bug: Version 14.17.0 funktioniert nicht. npm kann nicht gefunden werden.
    version.set("14.11.0")
    download.set(true)
}

tasks.register<NpxTask>("generateVueApp") {

    val npmInstall = tasks.named(NpmInstallTask.NAME)
    dependsOn(npmInstall)

    command.set("vue-cli-service")
    args.set(listOf("build", "--dest", destination))
    inputs.files("package.json", "package-lock.json", "vue.config.js", "babel.config.js")
    inputs.dir("src")
    inputs.dir(fileTree("node_modules").exclude(".cache"))
    outputs.dir(destination)

    doFirst {
        val path = System.getenv("PATH")
        logger.info("PATH = $path")
    }
}

