plugins {
    id("wolkenschloss.testbed")
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
}