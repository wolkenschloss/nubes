import com.github.gradle.node.npm.task.NpxTask

plugins {
    id("wolkenschloss.conventions.webapp")
}

tasks {
    named<NpxTask>("vue") {
        inputs.files("babel.config.js")
    }
}
