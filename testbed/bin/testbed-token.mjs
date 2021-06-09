import commander from "commander";
import {execp} from "./tools.mjs";

import config from "./config.js";
import {Address4} from "ip-address";
import chalk from "chalk";

async function run() {
    try {
        const ip4 = new Address4(config.testbed.ip)
        const cmd = `ssh ${ip4.addressMinusSuffix} 'microk8s kubectl -n kube-system get secret -o jsonpath="{.data.token}" $(microk8s kubectl -n kube-system get secret | grep default-token | cut -d" " -f1) | base64 --decode'`
        console.log(chalk.yellow("testbed-token run"))
        const {stdout} = await execp(cmd)

        const token = stdout;

        console.log(token)
    } catch (e) {
        console.log(chalk.red(e.stderr))
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
    .catch(console.error)