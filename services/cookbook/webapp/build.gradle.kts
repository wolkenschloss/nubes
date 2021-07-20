import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("wolkenschloss.conventions.webapp")
}
tasks {
    named<NpmTask>("npmInstall") {
        args.add("--silent")
    }
}

tasks {
    named<NpxTask>("vue") {
        inputs.files("cypress.config.js", "jest.config.json")
    }
}
