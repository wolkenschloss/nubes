import commander from "commander";
import {execp, logger} from "./tools.mjs";

import config from "./config.js";
import {Address4} from "ip-address";

async function run() {
    try {
        const ip4 = new Address4(config.testbed.ip)
        const cmd = `ssh ${ip4.addressMinusSuffix} 'microk8s kubectl -n kube-system get secret -o jsonpath="{.data.token}" $(microk8s kubectl -n kube-system get secret | grep default-token | cut -d" " -f1) | base64 --decode'`
        logger.debug("testbed-token")

        const {stdout} = await execp(cmd)

        console.log(stdout)
    } catch (e) {
        logger.error(e)
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

main()
    .then(() => {
    })
    .catch(() => {
    })