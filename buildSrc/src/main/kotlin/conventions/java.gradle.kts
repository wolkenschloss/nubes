package family.haschka.wolkenschloss.conventions

plugins {
    java
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    withType(JavaCompile::class) {
        options.compilerArgs.add("-Xlint:deprecation,unchecked")
        options.compilerArgs.add("-Werror")
    }
}
