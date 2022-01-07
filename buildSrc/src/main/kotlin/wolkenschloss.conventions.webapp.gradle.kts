import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask

plugins {
    id("wolkenschloss.conventions.java")
    id("com.github.node-gradle.node")
    id("java-library")
}

node {
    version.set("14.17.1")
    download.set(true)
    npmInstallCommand.set("ci")
}

val destination = file("$buildDir/classes/java/main/META-INF/resources/")

sourceSets {
    main {
        output.dir(destination, "builtBy" to listOf("vue"))
    }
}

tasks {
    val vue by register<NpxTask>("vue") {
        group = "build"
        description = "build vue application"
        dependsOn(NpmInstallTask.NAME)

        command.set("vue-cli-service")
        args.set(listOf("build", "--dest", destination.absolutePath))

        inputs.files("package.json", "package-lock.json", "vue.config.js")
            .withPropertyName("configFiles")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        inputs.dir("src")
            .withPropertyName("scripts")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        outputs.cacheIf { true }

        outputs.dir(destination)
            .withPropertyName("resources")
    }

    val unit by register<NpxTask>("unit") {
        group = "verification"
        description = "run jest unit tests"
        dependsOn(NpmInstallTask.NAME)

        command.set("vue-cli-service")
        args.set(listOf("test:unit"))

        inputs.files("package.json", "package-lock.json", "vue.config.js", "jest.config.json")
            .withPropertyName("configFiles")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        inputs.dir("src")
            .withPropertyName("scripts")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        inputs.dir("tests/unit")
            .withPropertyName("tests")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        outputs.cacheIf { true }
        outputs.dir("$buildDir/reports/tests/unit")
            .withPropertyName("reports")
    }

    val e2e by register<NpxTask>("e2e") {
        group = "verification"
        description = "run e2e tests"

        dependsOn(NpmInstallTask.NAME)
        command.set("vue-cli-service")
        args.set(listOf("test:e2e", "--headless"))

        inputs.files("package.json", "package-lock.json", "vue.config.js")
            .withPropertyName("configFiles")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        inputs.dir("src")
            .withPropertyName("scripts")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        inputs.dir("tests/e2e")
            .withPropertyName("tests")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        outputs.cacheIf { true }
        outputs.dir("$buildDir/reports/tests/e2e")
            .withPropertyName("reports")
    }

    named("build") {
        dependsOn(vue)
    }

    named("check") {
        dependsOn(unit, e2e)
    }
}

tasks {
    withType<NpmTask>().configureEach {
        logging.captureStandardOutput(LogLevel.INFO)
    }
    withType<NpxTask>().configureEach {
        logging.captureStandardOutput(LogLevel.INFO)
    }
}