#!/usr/bin/env node

import commander from "commander";
import {execp} from "./tools.mjs";
import config from "./config.js";
import build from "./build-config.mjs"

import {waitForPhoneHome} from "./phone-home.mjs";
import util from "util";
import path from "path";
import fs from "fs";
import {readKubernetesConfig} from "./testbed-config.mjs";
import {Address4} from "ip-address";
import chalk from "chalk";

async function isInactive() {
    const {stdout} = await execp('virsh list --inactive --name')
    console.log(stdout)
    return stdout.split('\n').map(line => line.trim()).includes(config.hostname)
}

async function start() {
    await execp(`virsh start ${config.hostname}`)
}

async function check() {
    await execp(`virsh dominfo ${config.hostname}`)
}

function backupKnownHosts() {
    const from = path.join(process.env.HOME, ".ssh", "known_hosts")
    const to = from + '.old'

    console.log(`backup file '${from}' to '${to}'`)
    return fs.promises.copyFile(from, to)
}

async function appendTestbedToKnownHosts(ip, server_key) {
    const known_hosts = path.join(process.env.HOME, ".ssh", "known_hosts")
    return fs.promises.appendFile(known_hosts, `${ip.addressMinusSuffix} ${server_key}`)
}

async function deleteTestbedFromKnownHosts(ip) {
    console.log(chalk.yellow("deleteTestbedFromKnownHosts"))
    return execp(`ssh-keygen -R ${ip.addressMinusSuffix}`)
}

function applyConfig(ip, name) {
    console.log(chalk.yellow("applyConfig()"))
    const filename = path.join(build.buildDir, name)
    return execp(`ssh ${ip.addressMinusSuffix} microk8s kubectl apply -f- < ${filename}`)
}

async function run() {

    const pWaitForCallback = util.promisify(waitForPhoneHome)

    try {
        await check()

        if (await isInactive()) {
            await start()

            const result = await pWaitForCallback(config.callback.port, config.callback.timeout)
            console.log("Result from Callback: %s, %o", result, result)
            const server_key = result['pub_key_ecdsa']
            console.log(`Server public ecdsa key: ${server_key}`)
            await backupKnownHosts()

            const testbed_ip = new Address4(config.testbed.ip)
            await deleteTestbedFromKnownHosts(testbed_ip)
            await appendTestbedToKnownHosts(testbed_ip, server_key)
            await applyConfig(testbed_ip, "dashboard-ingress.yaml")

            const kubernetes_config = await readKubernetesConfig()
            const run_dir = path.join(build.buildDir, "run")

            await fs.promises.mkdir(run_dir, {recursive: true})

            const config_file = path.join(run_dir, "config")

            await fs.promises.writeFile(config_file, kubernetes_config)
        } else {
            console.log("Already running")
            console.log(build)

        }
    } catch (err) {
        console.error(err.stderr || err)
        process.exitCode = 1
    }
}

async function main() {
    const program = new commander.Command()

    program.description("start testbed vm")
        .action(() => {
            run()
        })

    await program.parseAsync()
}

main().then(() => {}).catch(console.error)