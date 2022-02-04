
plugins {
    id("com.github.wolkenschloss.testbed")
    id("com.github.wolkenschloss.docker")
    id("com.github.wolkenschloss.ca")
}

defaultTasks("start")

testbed {
    base {
        name.set("ubuntu-20.04")
        url.set("https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img")
    }

    domain {
        name.set("testbed")
    }

    pool {
        name.set("testbed")
    }

    host {
        callbackPort.set(9292)
    }
}

tasks {
    register("createCa", wolkenschloss.gradle.ca.CreateTask::class.java) {
//        certificate.set(project.layout.buildDirectory.file("ca/ca.crt").map { it.asFile.toPath() })
//        privateKey.set(project.layout.buildDirectory.file("ca/ca.key").map { it.asFile.toPath() })
    }
}
