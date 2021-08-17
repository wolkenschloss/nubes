import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.npm.task.NpmTask

plugins {
   id("wolkenschloss.conventions.webapp")
    id("idea")
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


idea {
    module {
        excludeDirs.plusAssign(file("node_modules"))
        excludeDirs.plusAssign(file("dist"))
    }
}