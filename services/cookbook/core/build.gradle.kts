import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("family.haschka.wolkenschloss.conventions.core")
    kotlin("jvm") version "1.6.21"
    antlr
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.4"
}

group = "family.haschka.nubes.cookbook"
val buildSrcPackagePrefix = "family.haschka.wolkenschloss.cookbook"

tasks {
    generateGrammarSource {

        outputDirectory = project.layout.buildDirectory.file("generated-src/antlr/main/family/haschka/wolkenschloss/cookbook/parser").get().asFile
        arguments = arguments + listOf(
            "-visitor",
            "-no-listener",
            "-long-messages",
            "-package", "family.haschka.wolkenschloss.cookbook.parser"
        )
    }

    withType(KotlinCompile::class.java) {
        dependsOn(generateGrammarSource)
    }
}

dependencies {
    antlr(libs.antlr4)

    testImplementation(platform(libs.kotest5.bom))
    testImplementation(libs.kotest5.runner)
    testImplementation(libs.kotest5.assertions)
    testImplementation(libs.kotest5.datatest)
}

idea {
    module {
        settings {
            packagePrefix["src/main/kotlin"] = buildSrcPackagePrefix
            packagePrefix["src/test/kotlin"] = buildSrcPackagePrefix
        }
    }
}