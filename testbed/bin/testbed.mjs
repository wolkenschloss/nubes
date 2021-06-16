#!/usr/bin/env node

// Download Cloud Images from
// https://cloud-images.ubuntu.com/focal/current/
import {Command} from 'commander/esm.mjs'

const program = new Command()

program.version('0.0.1')
    .command('build', 'The command creates all of the files required for a virtual machine with a test bed.')
    .command('deploy', 'Provides a test bench as a virtual machine')
    .command('start', 'starts testbed vm')
    .command('token', 'Issues the token that can be used to log in to the kubernetes dashboard')
    .command('config', 'Prints out the Kubernetes configuration')

program.parse(process.argv)
// console.log("Program initialized")
