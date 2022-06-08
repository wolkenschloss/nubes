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
            "-long-messages",
            "-package", "family.haschka.wolkenschloss.cookbook.parser"
        )
    }

    withType(KotlinCompile::class.java) {
        dependsOn(generateGrammarSource)
    }

    val printSourceSetInformation by registering {
        doLast {
            sourceSets.forEach { srcSet ->
                println("[${srcSet.name}]")
                println("-->Source directories: ${srcSet.allJava.srcDirs}")
                println("-->Output directories: ${srcSet.output.classesDirs.files}")
                println("  [antlr directory]")
                println("  destinationDirectory: ${srcSet.antlr.destinationDirectory}")
                println("  classesDirectory: ${srcSet.antlr.classesDirectory}")
                println("  srcDirs: ${srcSet.antlr.srcDirs}")
                println()
            }
        }
    }
}

dependencies {
//    antlr("org.antlr:antlr4:4.10.1")
    antlr("org.antlr:antlr4:4.9.2")
    implementation("org.antlr:antlr4-runtime:4.9.2")

    testImplementation(platform(libs.kotest5.bom))
    testImplementation(libs.kotest5.runner)
    testImplementation(libs.kotest5.assertions)
    testImplementation(libs.kotest5.datatest)
}


tasks {

}


idea {
    module {
////        jdkName = "17"
//
        settings {
            packagePrefix["src/main/kotlin"] = buildSrcPackagePrefix
////            packagePrefix["src/test/integration/kotlin"] = buildSrcPackagePrefix
////            packagePrefix["src/test/functional/kotlin"] = buildSrcPackagePrefix
            packagePrefix["src/test/kotlin"] = buildSrcPackagePrefix
////            packagePrefix["src/test/fixtures/kotlin"] = buildSrcPackagePrefix
//            packagePrefix["build/generated-src/antlr/main"] = "$buildSrcPackagePrefix.parser"
        }
    }
}