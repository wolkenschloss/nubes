import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask

plugins {
    id("wolkenschloss.conventions.java")
    id("com.github.node-gradle.node")
    id("java-library")
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")

node {
    libs.findVersion("node").ifPresent {
        version.set(it.requiredVersion)
    }

    download.set(true)

    if (System.getenv().keys.contains("CI")) {
        npmInstallCommand.set("ci")
    } else {
        npmInstallCommand.set("install")
    }
}

val destination by extra(project.layout.buildDirectory.dir("classes/java/main/META-INF/resources/"))

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
        args.set(listOf(
            "build",
            "--silent",
            "--dest",
            destination.get().asFile.absolutePath)
        )

        inputs.files("package.json", "package-lock.json", "vue.config.js")
            .withPropertyName("configFiles")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        inputs.dir("src")
            .withPropertyName("scripts")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        inputs.dir("public")
            .withPropertyName("public")
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

    named<NpmTask>(NpmInstallTask.NAME) {
        args.addAll("--no-audit", "--fund=false", "--loglevel=error")
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