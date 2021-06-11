const fs = require('fs')
const path = require('path')
const {spawn} = require('child_process');
const xmlParser = require('fast-xml-parser');
const chalk = require('chalk');

function ensureString(buffer) {
    return buffer?.toString().trim()
}

function curry(fn, head) {
    return (...tail) => {
        return fn(head, ...tail);
    }
}

function parseResult(data) {
    return Object.assign(
        new StoragePool(),
        xmlParser.parse(ensureString(data))['pool'])
}

function spawner(cmd, args, onSuccess, onFailure) {
    const tool = spawn(cmd, args)
    console.log("Execute %s", tool.spawnargs.join(' '))
    return new Promise((resolve, reject) => {
        tool.on('exit', code => {
            console.log(`1> ${tool.spawnfile} exit code ${code}`)
            if (code === 0) {
                tool.stdout.on('data', data => {
                    console.log(`2> ${tool.spawnfile} exit code ${code}`)
                    onSuccess?.({resolve, reject, data: data.toString(), code}) || resolve()
                })
            } else {
                tool.stderr.on('data', data => {
                    console.log(`3> ${tool.spawnfile} exit code ${code}`)
                    onFailure?.({resolve, reject, data: data.toString(), code}) || reject(Error(data.toString()))
                })
            }
        })
        tool.on('error', error => {
            reject(Error(error.toString()))
        })
        tool.on('close', code => {
            console.log(`4> ${tool.spawnfile} exit code ${code}`)
            if(code !== 0) {
                reject(Error(`5> ${tool.spawnfile} exit with ${code}`))
            } else {
                console.log(`6> ${tool.spawnfile} exit code ${code}`)
                resolve(code)
            }
        })
    });
}

const virsh =  curry(spawner, 'virsh')
const cloud_localds = curry(spawner, 'cloud-localds')
const qemu_img = curry(spawner, 'qemu-img')
const virt_install = curry(spawner, 'virt-install')

class StoragePool {
    static load(poolName) {
        return virsh(['pool-dumpxml', poolName],
            context => context.resolve(parseResult(context.data))
        )
    }
}

function domainExists(domainname) {
    return virsh(['list', '--name', '--all'],
        context => context.resolve(
            context.data.trim().split('\n').includes(domainname)
        ))
}

async function readPublicSshKey() {
    const keyPath = path.join(process.env.HOME, '.ssh', 'id_rsa.pub')
    return fs.readFileSync(keyPath, 'utf8')
}

function configs(pool) {

    return {
        // Das Verzeichnis, in des die Festplattenabbilder geschrieben werden
        get buildDir() {
            return path.resolve(pool.target.path)
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
function createUserTemplate(instanz, ssh_key, user) {
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
      - ${ssh_key}
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
    await fs.promises.writeFile(filename, await content)
}

function createNetworkConfig(config) {
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

async function createCloudLocalDataSource(config) {


    return cloud_localds([
            '--network-config',
            config.networkConfigFile,
            config.cloudInitDataImage,
            config.userDataConfig,
            "--verbose"
        ]
    )
}

async function createRootImage(config) {
    return await qemu_img([
        "create",
        "-f", "qcow2",
        "-F", "qcow2",
        "-b", config.serverImage,
        config.rootImage
    ])
}

function resizeRootImage(config) {
    return qemu_img( ['resize', config.rootImage, '20G'])
}

function installAndStartVirtualMachine(config) {

    return virt_install( [
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
    ])
}

StoragePool.load('wolkenschloss')
    .then(async pool => {
        const config = configs(pool)

        if (await domainExists(config.instanz)) {
            throw Error("Domain exists")
        }

        await fs.promises.mkdir(config.buildDir, {recursive: true})
        await fs.promises.mkdir(config.generatedConfiguration, {recursive: true})

        await writeConfig(config.networkConfigFile, createNetworkConfig(config))
        const sshKey = await readPublicSshKey()
        await writeConfig(config.userDataConfig, createUserTemplate(config.instanz, sshKey, process.env.USER))
        await createCloudLocalDataSource(config)
        await createRootImage(config)
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
