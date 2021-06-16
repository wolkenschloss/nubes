#!/usr/bin/env node

import Mustache from 'mustache'
import * as path from 'path'
import * as fs from 'fs'
import config from './config.js'
import build_config from './build-config.mjs'
import {cloud_localds, execp, StoragePool, virsh} from "./tools.mjs";
import commander from "commander";
import winston from 'winston'

const logger = winston.createLogger({
    level: 'debug',
    format: winston.format.combine(winston.format.splat(), winston.format.cli()),
    transports: [
        new winston.transports.Console()
    ]
})

Mustache.escape = text => text

async function createCloudLocalDataSource(buildconfig, config) {
    logger.info("creating cloud-init data source")
    return cloud_localds([
            '--network-config',
            path.join(buildconfig.buildDir, "network-config"),
            path.join(buildconfig.buildDir, "pool", config.disks.cidata),
            path.join(buildconfig.buildDir, "user-data"),
            "--verbose"
        ]
    )
}

async function createRootImage(buildconfig, config, image) {
    logger.info("creating root image '%s'", config.disks.root)
    const target = path.join(buildconfig.buildDir, "pool", config.disks.root)
    await execp(`qemu-img create -f qcow2 -F qcow2 -b ${image} ${target}`)
    // return await qemu_img([
    //     "create",
    //     "-f", "qcow2",
    //     "-F", "qcow2",
    //     "-b", image,
    //     path.join(buildconfig.buildDir, "pool", config.disks.root)
    // ])
}

// Sucht ein Image in den Pools.
async function findImage(image) {
    logger.info("looking for image '%s'", image)

    if (path.isAbsolute(image)) {
        return image
    }

    const pools = await virsh(['pool-list', '--all', '--name'], async context => {
        context.resolve(context.data.split('\n')
            .map(poolname => poolname.trim())
            .filter(poolname => poolname !== ""))
    })

    const storage = await Promise.all(pools.map(pool => StoragePool.load(pool)))

    const paths = await Promise.all(storage.map(async s => {
        const target = s.target.path;
        try {
            const files = await fs.promises.readdir(target)
            for (const file of files) {
                if (file === image) {
                    return path.join(target, file)
                }
            }
            return undefined
        } catch (e) {
            return undefined
        }
    }))
    return paths.find(f => f !== undefined)
}

async function getDefaultNetworkDevice() {
    const {stdout} = await execp('ip route show default | cut -d" " -f5')
    return stdout.trim()
}

async function getDnsServer(device) {
    const {stdout} = await execp(`resolvectl dns ${device} | cut -d" " -f4`)
    return stdout.trim()
}

async function getIpAddress(device) {
    const {stdout} = await execp(`ip -json -4 address show dev ${device}`)
    return JSON.parse(stdout)[0]['addr_info'][0].local;
}

// 1. Alle Vorlagen aus dem Quellverzeichnis buildConfig.srcDir
//    in das Ausgabeverzeichnis buildConfig.buildDir kopieren.
//    Dabei alle Platzhalten mit den Werten aus testbedConfig
//    ersetzen.
//
// 2. Erstellen des Cloud-Init Konfigurationsvolume cidata.img.
//    In diesem Image befinden sich die Konfigurationsdatein
//    user-data und network-config. Cloud-Init benutzt diese
//    Dateien, um die virtuelle Maschine zu konfigurieren
//
// 3. Root Image erstellen. Das Root Image ist ein Overlay eines
//    geeigneten Betriebssystem Images. Es muss sich um ein
//    Cloud-Init fähiges Betriebssystem Image handeln.
//    Download Cloud Images from
//    https://cloud-images.ubuntu.com/focal/current/
async function run(buildConfig, testbedConfig) {
    try {
        logger.debug("mkdir %s", buildConfig.buildDir)
        await fs.promises.mkdir(buildConfig.buildDir, {recursive: true})

        logger.info("updating configuration")
        const device = await getDefaultNetworkDevice()
        logger.debug("default network device: %s", device)

        const dns = await getDnsServer(device)
        testbedConfig.testbed.nameserver = dns
        logger.debug("dns server: %s", dns)


        logger.debug("reading files from %s", buildConfig.srcDir)
        const files = await fs.promises.readdir(buildConfig.srcDir)

        testbedConfig.getSshKey = function () {
            return fs.readFileSync(this.ssh_key).toString().trim()
        }

        testbedConfig.callback.ip = await getIpAddress(device)
        logger.debug("my ip address: %s", testbedConfig.callback.ip)

        logger.info("processing files in '%s'", buildConfig.srcDir)
        for (const file of files) {
            logger.debug("processing file %s", file)
            const source = path.join("src", file)

            if (path.extname(file) === ".mustache") {


                const buffer = await fs.promises.readFile(source)

                const result = Mustache.render(buffer.toString(), testbedConfig)

                const parsedPath = path.parse(file)
                const target = path.join(buildConfig.buildDir, parsedPath.name)

                await fs.promises.writeFile(target, result, {mode: 0o664})
                logger.debug("source file %s rendered to %s", source, target)
            } else {

                const target = path.join(buildConfig.buildDir, file)

                await fs.promises.copyFile(source, target)
                logger.debug("source file %s copied to %s", source, target)
            }
        }

        const poolDir = path.join(buildConfig.buildDir, "pool")
        await fs.promises.mkdir(poolDir, {recursive: true})
        logger.info("pool directory created")

        await createCloudLocalDataSource(buildConfig, config)

        const image = await findImage(config.image)
        await createRootImage(buildConfig, config, image)

        logger.info("resizing root image")
        await execp(`qemu-img resize ${path.join(buildConfig.buildDir, 'pool', config.disks.root)} 20G`)
    } catch (err) {
        logger.error(err)
        process.exitCode = 1
    }
}

const program = new commander.Command()

async function main() {
    program.name('build')
        .description('create all images for testbed virtual machine')
        .action(() => {
            run(build_config, config)
        })

    await program.parseAsync(process.argv)
}

main().then(() => {}).catch(() => {})
