import {exec, spawn} from "child_process";
import xmlParser from "fast-xml-parser";
import util from "util";

const execp = util.promisify(exec)

import winston from 'winston'

const logger = winston.createLogger({
    level: 'debug',
    format: winston.format.combine(winston.format.splat(), winston.format.cli()),
    transports: [
        new winston.transports.Console()
    ]
})

function curry(fn, head) {
    return (...tail) => {
        return fn(head, ...tail);
    }
}

function spawner(cmd, args, onSuccess, onFailure) {
    const tool = spawn(cmd, args)
    // console.log("Execute %s", tool.spawnargs.join(' '))
    return new Promise((resolve, reject) => {
        tool.on('exit', code => {
            // console.log(`1> ${tool.spawnfile} exit code ${code}`)
            if (code === 0) {
                tool.stdout.on('data', data => {
                    // console.log(`2> ${tool.spawnfile} exit code ${code}`)
                    onSuccess?.({resolve, reject, data: data.toString(), code}) || resolve()
                })
            } else {
                tool.stderr.on('data', data => {
                    // console.log(`3> ${tool.spawnfile} exit code ${code}`)
                    onFailure?.({resolve, reject, data: data.toString(), code}) || reject(Error(data.toString()))
                })
            }
        })
        tool.on('error', error => {
            reject(Error(error.toString()))
        })
        tool.on('close', code => {
            // console.log(`4> ${tool.spawnfile} exit code ${code}`)
            if(code !== 0) {
                reject(Error(`5> ${tool.spawnfile} exit with ${code}`))
            } else {
                // console.log(`6> ${tool.spawnfile} exit code ${code}`)
                resolve(code)
            }
        })
    });
}

const virsh =  curry(spawner, 'virsh')
const cloud_localds = curry(spawner, 'cloud-localds')

function ensureString(buffer) {
    return buffer?.toString().trim()
}

function parseResult(data) {
    return Object.assign(
        new StoragePool(),
        xmlParser.parse(ensureString(data))['pool'])
}

class StoragePool {
    static load(poolName) {
        return virsh(['pool-dumpxml', poolName],
            context => context.resolve(parseResult(context.data))
        )
    }
}

export {curry, spawner, virsh, cloud_localds, StoragePool, execp, logger}