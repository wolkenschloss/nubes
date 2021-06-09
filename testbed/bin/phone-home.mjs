import fastify from "fastify";
import formbody from 'fastify-formbody'

async function waitForPhoneHome(port, timeout, callback) {

    const app = fastify({logger: true})
    app.register(formbody)

    const handle = setTimeout(() => {
        app.close(() => console.log("Server wird beendet"))
        callback(Error("No callback from virtual machine"))
    }, timeout)

    function close() {
        clearTimeout(handle)
        app.close(() => console.log("Server wird heruntergefahren"))
    }

    app.post("/", (req, res) => {
        console.log(req.body)
        res.status(200).send()
    })

    app.post("/:ip", (req, res) => {
        const body = req.body
        res.status(200).send()
        close()
        callback(undefined, body)
    })

    app.listen(port, '0.0.0.0', (err, address) => {
        if (err) {
            callback(err)
        }
        console.log("Waiting for response from testbedvm")
    })
}

export {waitForPhoneHome}