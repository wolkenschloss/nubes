#!/usr/bin/env node

import buildconfig from './build-config.mjs'
import config from './config.js'

import {virsh} from "./tools.mjs";
import fs from "fs";
import path from "path";

// Deploy
//
// Erzeugt Pool, Domain und Images.
//
// Wenn die Domain existiert und eingeschaltet ist, wird sie heruntergefahren
// Wenn der Pool existiert, wird er angehalten.
// Wenn das Verzeichnis des Pools nicht exisistiert, wird es erzeugt
// Das cidata Image wird im Pool Verzeichnis erzeugt.
// Das root Image wird im Pool Verzeichnis erzeugt und vergrößert.
// Wenn der Pool nicht existiert, wird er angelegt.
// Wenn die Domain nicht existiert, wird sie angelegt.
//
// Pool und Domain werden nicht gestartet!

function poolExists(poolname) {
    return virsh(['pool-list', '--name', '--all'], context => {
        context.resolve(context.data.trim().split('\n').map(pool => pool.trim())
            .reduce((a, b) => a || b === poolname, false))
    })
}

function domainExists(domainname) {
    return virsh(['list', '--name', '--all'],
        context => context.resolve(
            context.data.trim().split('\n').includes(domainname)
        ))
}

async function run(buildConfig) {
    await fs.promises.mkdir(config.pool.directory, {recursive: true})
    // ...

    // Disk Images in das Pool-Verzeichnis kopieren
    const files = await fs.promises.readdir(path.join(buildconfig.buildDir, "pool"), {withFileTypes: true})

    await Promise.all(files.filter(f => f.isFile())
        .map(f => {
            const source = path.join(buildconfig.buildDir, "pool", f.name)
            const target = path.join(config.pool.directory, f.name)
            console.log(`copy ${f.name} to ${path.join(config.pool.directory, f.name)}`)
            fs.promises.copyFile(source, target)
        }))

    // Pool in libvirt anlegen, nicht starten!
    if (! await poolExists(config.pool.name)) {
        await virsh(['pool-define', path.join(buildconfig.buildDir, 'pool.xml')])
    }

    if (! await domainExists(config.image)) {
        await virsh(['define', path.join(buildconfig.buildDir, "testbed.xml")])
    }
}

// main(buildconfig).then(() => console.log("Fertig")).catch(console.error)

import commander from "commander";



async function main() {
    const program = new commander.Command()
    program.name('deploy')
        .action(() => {
            run(buildconfig)
        })
    await program.parseAsync()
}

main().then(() => {}).catch(console.error)