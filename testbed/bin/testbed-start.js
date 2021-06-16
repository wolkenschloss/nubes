#!/usr/bin/env node

import commander from "commander";
import {execp, readKubernetesConfig} from "./tools.mjs";
import config from "./config.js";
import build from "./build-config.mjs"

import {waitForPhoneHome} from "./phone-home.mjs";
import util from "util";
import path from "path";
import fs from "fs";
import {Address4} from "ip-address";

import {logger} from "./tools.mjs";

function dry(options, real, fake) {
    if (!options.dryRun) {
        return real()
    } else {
        return fake()
    }
}

async function isInactive() {
    const {stdout} = await execp('virsh list --inactive --name')
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

    logger.debug("Backup %s", from)
    return fs.promises.copyFile(from, to)
}

async function appendTestbedToKnownHosts(ip, server_key) {

    const known_hosts = path.join(process.env.HOME, ".ssh", "known_hosts")
    logger.debug("Appending server '%s' to %s", ip.addressMinusSuffix, known_hosts)

    return fs.promises.appendFile(known_hosts, `${ip.addressMinusSuffix} ${server_key}`)
}

async function deleteTestbedFromKnownHosts(ip) {
    logger.info("Deleting server '%s' from known_hosts", ip.addressMinusSuffix)
    return execp(`ssh-keygen -R ${ip.addressMinusSuffix}`)
}

function applyConfig(ip, name) {
    logger.debug("Apply manifest %s to kubernetes", name)
    const filename = path.join(build.buildDir, name)
    return execp(`ssh ${ip.addressMinusSuffix} microk8s kubectl apply -f- < ${filename}`)
}

async function saveKubernetesConfiguration() {
    const kubernetes_config = await readKubernetesConfig()
    const run_dir = path.join(build.buildDir, "run")

    await fs.promises.mkdir(run_dir, {recursive: true})

    const config_file = path.join(run_dir, "config")

    await fs.promises.writeFile(config_file, kubernetes_config)
}

async function updateKnownHosts(testbed_ip, server_key) {
    await backupKnownHosts()
    await deleteTestbedFromKnownHosts(testbed_ip)
    await appendTestbedToKnownHosts(testbed_ip, server_key)
}

async function run(options) {

    const pWaitForCallback = util.promisify(waitForPhoneHome)

    const dried = (real, fake) => dry(options, real, fake)

    try {
        logger.info("testbed start")
        logger.info("options: %o", options)


        await dried(check, () => {})

        if (await dried(isInactive, () => true)) {
            await dried(start, () => {})
            //
            const result = await dried(() => pWaitForCallback(config.callback.port, config.callback.timeout), () => {
                return {pub_key_ecdsa: 'any value'}
            })

            logger.debug("Got signal from testbed vm")

             const server_key = result['pub_key_ecdsa']
            logger.debug("Got public key from Server")

            const testbed_ip = new Address4(config.testbed.ip)
            await dried(() => updateKnownHosts(testbed_ip, server_key), () => {})
            await dried(() => applyConfig(testbed_ip, "dashboard-ingress.yaml"), () => {})
            await dried(saveKubernetesConfiguration, () => {})
        } else {
            logger.info("Testbed already running. Try 'testbed destroy && testbed start' to create a new one")
        }
    } catch (err) {
        logger.error(err)
        process.exitCode = 1
    }
}

async function main() {
    const program = new commander.Command()

    program.description("start testbed vm")
        .option('--dry-run', "The start of the testbed is simulated. No virtual machine is started in the process.")
        .action(options => {
            run(options)
        })

    await program.parseAsync()
}

main().then(() => {}).catch(() => {})