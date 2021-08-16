import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.npm.task.NpmTask

plugins {
   id("wolkenschloss.conventions.webapp")
}

tasks {
    named<NpmTask>(NpmInstallTask.NAME) {
        args.add("--silent")
    }
}

tasks {
    named<NpxTask>("e2e") {
        inputs.files("cypress.config.js")
    }
}
