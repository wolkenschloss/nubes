import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// see https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("family.haschka.wolkenschloss.conventions.core")
    alias(libs.plugins.kotlin.noarg)
    alias(libs.plugins.kotlin.allopen)
    antlr
}

noArg {
    annotations(
        "family.haschka.wolkenschloss.cookbook.Value",
        "family.haschka.wolkenschloss.cookbook.Aggregate",
        "family.haschka.wolkenschloss.cookbook.Event"
    )
}

allOpen {
    annotations("family.haschka.wolkenschloss.cookbook.Event")

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

    compileTestKotlin {
        dependsOn(generateTestGrammarSource)
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
