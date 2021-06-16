import fastify from "fastify";
import formbody from 'fastify-formbody'
import {logger} from "./tools.mjs";

async function waitForPhoneHome(port, timeout, callback) {

    const app = fastify()
    app.register(formbody)

    const handle = setTimeout(() => {
        app.close(() => logger.debug("Server closed"))
        callback(Error("No callback from virtual machine"))
    }, timeout)

    function close() {
        clearTimeout(handle)
        app.close(() => logger.debug("Server closed"))
    }

    app.post("/", (req, res) => {
        res.status(200).send()
    })

    app.post("/:ip", ({body}, res) => {
        res.status(200).send()
        close()
        callback(undefined, body)
    })

    app.listen(port, '0.0.0.0', (err) => {
        if (err) {
            callback(err)
        }

        logger.info("I am waiting for a message from the test bench. This may take several minutes.")
    })
}

export {waitForPhoneHome}