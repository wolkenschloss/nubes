import path from "path";

export default {
    home: process.env.HOME,
    testbed: {
        ip: "192.168.122.152/24",
        gateway: "192.168.122.1",
        nameserver: "1.1.1.1",
    },
    disks: {
        root: "root.qcow2",
        cidata: "cidata.img",
    },

    pool: {
        name: "wolkenschloss",
        directory: "/tmp/wolkenschloss"
    },

    get hostname() { return this.domain.split(".")[0] },
    domain: "testbed.local",
    user: process.env.USER,
    ssh_key: path.join(process.env.HOME, ".ssh", "id_rsa.pub"),
    callback: {
        port: 9191,
        timeout: 6000000 // 10 m
        // timeout: 60000 // 1m
    },

    image: "/tmp/wolkenschloss/focal-server-cloudimg-amd64-disk-kvm.img2"
}