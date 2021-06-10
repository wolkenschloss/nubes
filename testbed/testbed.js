const chalk = require('chalk')
const os = require('os')
const fs = require('fs')
const path = require('path')
const util = require('util')
const childProcess = require('child_process');
const xmlParser = require('fast-xml-parser');
const defaultGateway = require('default-gateway')

//# pool-dumpxml wolkenschloss

class StoragePool {

    static load (poolname) {
        return new Promise((resolve, reject) => {
            const virsh = childProcess.spawn('virsh', ['pool-dumpxml', poolname]);
            virsh.stdout.on('data', data => {
                const json = xmlParser.parse(data.toString())
                const instanz = new StoragePool()
                Object.assign(instanz, json.pool)
                resolve(instanz)
            })

            virsh.stderr.on('data', data => {
                reject(data)
            })

            virsh.on('close', code => {
                console.log(`virsh exit ${code}`)
            })

            virsh.on('error', error => {
                reject(error)
            })
        })
    }
}

async function readPublicSshKey() {
    const keyPath = path.join(process.env.HOME, '.ssh', 'id_rsa.pub')
    const data = await fs.readFileSync(keyPath, 'utf8')
    return data
}

// const config = {
//
//     // Das Verzeichnis, in des die Festplattenabbilder geschrieben werden
//     get buildDir(){ return path.resolve('build') },
//
//     // Verzeichnis, in dem Betriebssystemabbilder zwischengespeichert sind.
//     // Es eignen sich nur Betriebssysteme mit Cloud Init.
//     // https://cloud-images.ubuntu.com/
//     get cacheDir() {return path.resolve('.cache')},
//
//     // Das für den Bau des Testprüfstandes verwendete Abbild eines
//     // Betriebssystems.
//     get serverImage() {return path.join(this.cacheDir, "focal-server-cloudimg-amd64-disk-kvm.img")},
//
//     get generatedConfiguration() {return path.join(this.buildDir, "generated-config")},
//
//     get networkConfigFile() {return path.join(this.generatedConfiguration, "network-config")},
//
//     get userDataConfig() {return path.join(this.generatedConfiguration, "user-data")},
//
//     get cloudInitDataImage() {return path.join(this.buildDir, "cidata.img")},
//
//     get rootImage() {return path.join(this.buildDir, "root.qcow2")},
//
//     get gateway() { return defaultGateway.v4.sync().gateway },
//
//     ip: '192.168.122.153/24',
//
//     nameserver: '192.168.4.2',
//
//     instanz: "testbedvm",
// }

async function configs(pool$)  {

    const pool = await pool$

    return {
        // Das Verzeichnis, in des die Festplattenabbilder geschrieben werden
        get buildDir() {
            return path.resolve(pool.target.path)
        },

        // Verzeichnis, in dem Betriebssystemabbilder zwischengespeichert sind.
        // Es eignen sich nur Betriebssysteme mit Cloud Init.
        // https://cloud-images.ubuntu.com/
        get cacheDir() {
            return path.resolve('.cache')
        },

        // Das für den Bau des Testprüfstandes verwendete Abbild eines
        // Betriebssystems.
        get serverImage() {
            return path.join(this.buildDir, "focal-server-cloudimg-amd64-disk-kvm.img")
        },

        get generatedConfiguration() {
            return path.join(this.buildDir, "generated-config")
        },

        get networkConfigFile() {
            return path.join(this.generatedConfiguration, "network-config")
        },

        get userDataConfig() {
            return path.join(this.generatedConfiguration, "user-data")
        },

        get cloudInitDataImage() {
            return path.join(this.buildDir, "cidata.img")
        },

        get rootImage() {
            return path.join(this.buildDir, "root.qcow2")
        },


        gateway: '192.168.122.1',
        ip: '192.168.122.152/24',
        nameserver: '192.168.4.2',
        instanz: "testbedvm",
    }
}

// // Download Cloud Images from
// // https://cloud-images.ubuntu.com/focal/current/
//

async function createUserTemplate(instanz, ssh_key$, user) {
    console.log("create user-data template")
    return `#cloud-config

preserve_hostname: False
hostname: ${instanz}
fqdn: ${instanz}.haschka.family
manage_etc_hosts: True

ssh_pwauth: False

groups:
  - microk8s
  
users:
  - name: ${user}
    shell: '/bin/bash'
    groups: 
      - sudo
      - microk8s
    ssh_authorized_keys:
      - ${await ssh_key$}
    sudo: 'ALL=(ALL) NOPASSWD: ALL'

locale: ${process.env.LANG}

# Just for now
package_update: False
package_upgrade: False

packages:
  - tzdata
  - openssh-server
  - curl

snap:
  commands:
    01: snap install microk8s --classic
    02: microk8s status --wait-ready
    03: microk8s enable dns storage
`
}

async function writeConfig(filename, content$) {
    console.log(`write config to ${filename}`)
    await fs.promises.mkdir(path.dirname(filename) , {recursive: true})
    await fs.promises.writeFile(filename, await content$)
}


function createeNetworkConfig(config) {
    return `version: 2
ethernets:
  enp1s0:
    addresses: [${config.ip}]
    dhcp4: no
    dhcp6: no
    gateway4: ${config.gateway}
    nameservers:
        addresses: [${config.nameserver}]
`
}

async function createCloudLocalDataSource$(config) {
    await fs.promises.mkdir(path.dirname(config.cloudInitDataImage), {recursive: true})

    return new Promise((resolve, reject) => {
        const cloud_localds = childProcess.spawn('cloud-localds', [
            '--network-config',
            config.networkConfigFile,
            config.cloudInitDataImage,
            config.userDataConfig,
            "--verbose"
        ])
        cloud_localds.stdout.on('data', data => {
            console.log(`data`)
        })
        cloud_localds.stderr.on('data', data => {
            console.log(data.toString())
        })
        cloud_localds.on('close', code => {
            if (code === 0) {
                resolve(config)
            } else {
                reject(code)
            }
        })
    })
}

function createRootImage$(config) {
    return new Promise((resolve, reject) => {
        const qemu_img = childProcess.spawn('qemu-img', [
            "create",
            "-f", "qcow2",
            "-F", "qcow2",
            "-b", config.serverImage,
            config.rootImage
        ])

        qemu_img.stdout.on('data', (data) => {
            console.log(`qemu-img create> ${data}`)
        })
        qemu_img.stderr.on('data', (data) => {
            console.error(`qemu-img create> ${data}`)
        })
        qemu_img.on('close', code => {
            if (code !== 0) {
                reject(`qemu-img create: Exit with code ${code}`)
            }
            resolve(config)
        })
        qemu_img.on('error', error => {
            reject(error)
        })
    })
}

function resizeRootImage(config) {
    return new Promise((resolve, reject) => {
        const qemu_img = childProcess.spawn('qemu-img', ['resize', config.rootImage, "20G"])
        qemu_img.stdout.on('data', data => {console.log(`qemu-img resize> ${data}`)})
        qemu_img.stderr.on('data', data => {console.error(`qemu-img resize> ${data}`)})

        qemu_img.on('close', code => {
            console.log(`qemu-img resize: Exit with code ${code}`);
            if (code !== 0) {
                reject(`qemu-img resize exited with code ${code}`)
            }
            resolve(config)
        })
    })
}

function installAndStartVirtualMachine(config) {
    return new Promise((resolve, reject) => {
        const virt_install = childProcess.spawn('virt-install', [
            '--connect', 'qemu:///system',
            // '--unattended',
            '--noautoconsole',
            '--virt-type', 'kvm',
            '--name', config.instanz,
            '--ram', '4096',
            '--vcpus', '2',
            '--os-type', 'linux',
            '--os-variant', 'ubuntu20.04',
            '--disk', `${config.rootImage},device=disk,bus=virtio`,
            '--disk', `${config.cloudInitDataImage},format=raw`,
            '--import',
            '--network', 'network=default',
            '--graphics', 'none'
        ])

            // <interface type="network">
            // <mac address="52:54:00:a6:ba:fd"/>
            // <source network="default" portid="1a4b5cb0-ae92-49c0-a52a-d7c227a61833" bridge="virbr0"/>
            // <target dev="vnet0"/>
            // <model type="virtio"/>
            // <alias name="net0"/>
            // <address type="pci" domain="0x0000" bus="0x01" slot="0x00" function="0x0"/>
            // </interface>

        virt_install.stdout.on('data', data => {console.log(`virt-install> ${data}`)})
        virt_install.stderr.on('data', data => {console.error(`virt-install> ${data}`)})
        virt_install.on('close', code => {
            console.log(`virt-install exit with code ${code}`)
            if (code == 0) {
                resolve()
            } else {
                reject(code)
            }
        })
    })
}

// createImages$
//     .then(resizeRootImage)
//     .then(installAndStartVirtualMachine)
//     .catch(error => {console.error(`Fehler: ${error}`)})
//     .finally(() => console.log("Progammende"))

StoragePool.load('wolkenschloss')
    .then(async pool => {
        console.log(`I've got the pool ${pool.target.path}`)
        return await configs(pool)
    })
    .then(async config => {
        console.log(`Got Config: ${config}`)
        console.log(config)
        const networkConfig$ = await writeConfig(config.networkConfigFile, createeNetworkConfig(config))
        const userData$ = await writeConfig(config.userDataConfig, createUserTemplate(config.instanz, readPublicSshKey(), process.env.USER))
        return Promise.all([config, networkConfig$, userData$])
    })
    .then(([config, nix, da]) => {
        console.log("Schritt 42")
        console.log(config)
        return createCloudLocalDataSource$(config)
    })
    .then(createRootImage$)
    .then(resizeRootImage)
    .then(installAndStartVirtualMachine)
    .then(() => {console.log("Geschafft.")})
    .catch(console.error)
    .finally(() => console.log("Done"))



//     .then(()=> {
//         const virt_install = childProcess.spawn('virt-install', [
//             '--connect', 'qemu:///system',
//             '--virt-type', 'kvm',
//             '--name', instanz,
//             '--ram', '4096',
//             '--vcpus', '2',
//             '--os-type', 'linux',
//             '--os-variant', 'ubuntu20.04',
//             '--disk', `${rootImage},device=disk,bus=virtio`,
//             '--disk', `${cloudInitDataImage},format=raw`,
//             '--import',
//             '--network', 'network=default',
//             '--graphics', 'none'
//         ])
//
//         // virt_install.stdout.on('data', data => {console.log(`virt-install> ${data}`)})
//         // virt_install.stderr.on('data', data => {console.error(`virt-install> ${data}`)})
//         virt_install.on('close', code => {
//             console.log(`virt-install exit with code ${code}`)
//
//         })
//     })
//     .catch(err => {
//         console.error("Der Große Error Handler")
//         console.error(err)
//     })
//     .finally(() => {
//         console.log("Programende")
//     })
//
//
// // fs.writeFileSync(`${temp_dir}/user-data`, createUserTemplate(sshKey))
// //
// // console.log(createUserTemplate("testbedvm", sshKey))
// // console.log(readPublicSshKey(process.env.USER))
//
// // create disk image for new vm => gradle build
//
// // start vm => gradle quarkusDev