import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings

plugins {
    id("family.haschka.wolkenschloss.conventions.core")
    kotlin("jvm") version "1.6.21"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.4"
}

group = "family.haschka.nubes.cookbook"
val buildSrcPackagePrefix = "family.haschka.wolkenschloss.cookbook"

dependencies {
    testImplementation(platform(libs.kotest5.bom))
    testImplementation(libs.kotest5.runner)
    testImplementation(libs.kotest5.assertions)
    testImplementation(libs.kotest5.datatest)
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
        }
    }
}