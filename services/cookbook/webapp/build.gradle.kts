import com.github.gradle.node.npm.task.NpxTask

plugins {
   id("family.haschka.wolkenschloss.conventions.webapp")
    id("idea")
}

tasks {
    named<NpxTask>("e2e") {
        inputs.files("cypress.config.js")
    }
}


idea {
    module {
        excludeDirs.plusAssign(file("dist"))
    }
}