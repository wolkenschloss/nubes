plugins {
    id("com.github.wolkenschloss.testbed")
}

testbed {
    domain {
        name.set("launch")
        hosts.addAll("dummy1", "dummy2")
        disk.set("10G")
        mem.set("2G")
        cpus.set(1)
        image.set("focal")
    }
}