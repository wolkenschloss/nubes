plugins {
    java
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

tasks {
    val projectProperties by registering(WriteProperties::class) {
        group = "other"
        description = "Write project properties in a file."
        outputFile = file("${buildDir}/project.properties")
        encoding = "UTF-8"
        comment = "generated by wolkenschloss.conventions.java.gradle.kts"

        properties(mapOf(
                "project.name" to project.name,
                "project.group" to project.group,
                "project.version" to project.version,
        ))

        project.findProperty("vcs.commit")?.apply {
            property("vcs.commit", this)
        }

        project.findProperty("vcs.ref")?.apply {
            property("vcs.ref", this)
        }
    }

    processResources {
        from(projectProperties)
    }
}
