#!/usr/bin/env node
import commander from "commander";
import {readKubernetesConfig} from "./tools.mjs";

async function main() {
    const program = new commander.Command()

    .action(async () => {
            console.log(await readKubernetesConfig())
        })

    await program.parseAsync()
}

main().then(() => {}).catch(() => {})

