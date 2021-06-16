#!/usr/bin/env node
import commander from "commander";
import {execp} from "./tools.mjs";

import config from "./config.js";
import {Address4} from "ip-address";
import {logger} from "./tools.mjs";

async function readKubernetesConfig() {
    try {
        logger.debug("testbed config")

        const ip4 = new Address4(config.testbed.ip)
        const cmd = `ssh ${ip4.addressMinusSuffix} 'microk8s config'`

        const {stdout} = await execp(cmd)

        return stdout.trim();
    } catch (e) {
        logger.error(e)
        process.exitCode = 1
    }
}

async function main() {
    const program = new commander.Command()

    .action(async () => {
            console.log(await readKubernetesConfig())
        })

    await program.parseAsync()
}

main().then(() => {}).catch(() => {})

export {readKubernetesConfig}