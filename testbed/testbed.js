const fs = require('fs')
const path = require('path')
const {spawn} = require('child_process');
const xmlParser = require('fast-xml-parser');
const chalk = require('chalk');

function ensureString(buffer) {
    return buffer?.toString().trim()
}

function parseResult(data) {
    return Object.assign(
        new StoragePool(),
        xmlParser.parse(ensureString(data)).pool)
}

function spawner(tool, onSuccess, onFailure) {
    console.log("Execute %s", tool.spawnargs.join(' '))
    return new Promise((resolve, reject) => {
        tool.on('exit', code => {
            if (code === 0) {
                tool.stdout.on('data', data => {
                    onSuccess?.({resolve, reject, data: data.toString(), code}) || resolve()
                })
            } else {
                tool.stderr.on('data', data => {
                    onFailure?.({resolve, reject, data: data.toString(), code}) || reject(Error(data.toString()))
                })
            }
        })
    });
}

function virsh(command, parameter, onSuccess, onFailure) {
    return spawner(spawn('virsh', [command, ...parameter]),
        context => onSuccess?.(context) || context.resolve(),
        context => onFailure?.(context) || context.reject(Error(context.data)))
}

class StoragePool {
    static load(poolName) {
        return virsh('pool-dumpxml', [poolName],
            context => context.resolve(parseResult(context.data))
        )
    }
}

async function domainExists(domainname) {
    return await virsh('list', ['--name', '--all'],
        context => context.resolve(
            context.data.trim().split('\n').includes(domainname)
        ))
}

async function readPublicSshKey() {
    console.log("Reading public ssh key")
    const keyPath = path.join(process.env.HOME, '.ssh', 'id_rsa.pub')
    const data = await fs.readFileSync(keyPath, 'utf8')
    return data
}

async function configs(pool) {

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
async function createUserTemplate(instanz, ssh_key$, user) {
    console.log("Creating user-data")
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
  - acpid
  - tzdata
  - openssh-server
  - curl

snap:
  commands:
    01: snap install microk8s --classic
    02: microk8s status --wait-ready
    03: microk8s enable dns storage ingress dashboard
`
}

async function writeConfig(filename, content) {
    await fs.promises.mkdir(path.dirname(filename), {recursive: true})
    await fs.promises.writeFile(filename, await content)
}

function createeNetworkConfig(config) {
    console.log("Creating network-config")
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
        const cloud_localds = spawn('cloud-localds', [
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

    return spawner(spawn('qemu-img', [
        "create",
        "-f", "qcow2",
        "-F", "qcow2",
        "-b", config.serverImage,
        config.rootImage
    ]))
}

function resizeRootImage(config) {
    return spawner(spawn('qemu-img', ['resize', config.rootImage, '20G']))

}

function installAndStartVirtualMachine(config) {

    return spawner(spawn('virt-install', [
        '--connect', 'qemu:///system',
        '--description', "Wolkenschloss Testbed",
        '--noautoconsole',
        '--wait', "0",
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
    ]))
}

StoragePool.load('wolkenschloss')
    .then(pool => {
        console.log("Reading configuration")
        return configs(pool)
    })
    .then(async config => {
        console.log("Creating VM configuration files")

        if (await domainExists(config.instanz)) {
            throw Error("Domain exists")
        }

        await writeConfig(config.networkConfigFile, createeNetworkConfig(config))
        await writeConfig(config.userDataConfig, createUserTemplate(config.instanz, readPublicSshKey(), process.env.USER))
        await createCloudLocalDataSource$(config)
        await createRootImage$(config)
        await resizeRootImage(config)
        await installAndStartVirtualMachine(config)
    })
    .then(() => {
        console.log(chalk.green("Geschafft."))
    })
    .catch(error => {
        console.error(chalk.red(error));
    })
    .finally(() => console.log(chalk.green("Done")))