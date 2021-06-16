#!/usr/bin/env node

import buildconfig from './build-config.mjs'
import config from './config.js'
import commander from "commander";
import {execp, logger} from "./tools.mjs";
import fs from "fs";
import path from "path";

// Deploy
//
// Erzeugt Pool, Domain und Images.
//
// Wenn die Domain existiert und eingeschaltet ist, wird sie heruntergefahren
// Wenn der Pool existiert, wird er angehalten.
// Wenn das Verzeichnis des Pools nicht existiert, wird es erzeugt
// Das cidata Image wird im Pool Verzeichnis erzeugt.
// Das root Image wird im Pool Verzeichnis erzeugt und vergrößert.
// Wenn der Pool nicht existiert, wird er angelegt.
// Wenn die Domain nicht existiert, wird sie angelegt.
//
// Pool und Domain werden nicht gestartet!
async function poolExists(poolName) {

    const {stdout} = await execp(`virsh pool-list --name --all`)

    return stdout.trim()
        .split('\n')
        .map(pool => pool.trim())
        .reduce((a, b) => a || b === poolName, false)
}

async function domainExists(domainname) {

    const { stdout } = await execp('virsh list --name --all')

    return stdout.trim().split('\n').includes(domainname)
}

async function copyImages() {
    // Disk Images in das Pool-Verzeichnis kopieren
    const files = await fs.promises.readdir(path.join(buildconfig.buildDir, "pool"), {withFileTypes: true})

    await Promise.all(files.filter(f => f.isFile())
        .map(async f => {
            const source = path.join(buildconfig.buildDir, "pool", f.name)
            const target = path.join(config.pool.directory, f.name)

            logger.debug("copy %s to %s", source, target)

            await fs.promises.copyFile(source, target)
        }))
}

async function run(buildConfig) {
    try {
        logger.info("testbed deploy")

        logger.debug("Creating pool directory '%s'", config.pool.directory)
        await fs.promises.mkdir(config.pool.directory, {recursive: true})

        await copyImages();

        // Pool in libvirt anlegen, nicht starten!
        if (!await poolExists(config.pool.name)) {
            logger.debug("Creating pool '%s'", config.pool.name)
            await execp(`virsh pool-define ${path.join(buildConfig.buildDir, 'pool.xml')}`)
        }

        if (!await domainExists(config.hostname)) {
            logger.debug("Defining domain '%s'", config.hostname)
            await execp(`virsh define ${path.join(buildConfig.buildDir, "testbed.xml")}`)
        }

    } catch (error) {
        logger.error(error)
        process.exitCode = 1
    }
}

async function main() {
    const program = new commander.Command()
    program.name('deploy')
        .action(() => {
            run(buildconfig)
        })
    await program.parseAsync()
}

main().then(() => {}).catch(() => {})