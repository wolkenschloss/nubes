import com.github.gradle.node.npm.task.NpxTask

plugins {
   id("wolkenschloss.conventions.webapp")
    id("idea")
}

tasks {
    named<NpxTask>("e2e") {
        inputs.files("cypress.config.js")
    }
}


idea {
    module {
//        excludeDirs.plusAssign(file("node_modules"))
        excludeDirs.plusAssign(file("dist"))
    }
}