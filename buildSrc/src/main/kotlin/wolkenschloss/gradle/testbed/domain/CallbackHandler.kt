package wolkenschloss.gradle.testbed.domain

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.gradle.api.logging.Logger
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.BlockingQueue

internal class CallbackHandler(private val server: BlockingQueue<String>, private val logger: Logger) : HttpHandler {
    @Throws(IOException::class)
    override fun handle(exchange: HttpExchange) {
        val body = String(exchange.requestBody.readAllBytes(), StandardCharsets.UTF_8)
        val data = FormData(body)
        val serverKey: String = data.value("pub_key_ecdsa")
        if (server.offer(serverKey)) {
            exchange.sendResponseHeaders(200, 0)
            logger.info("Response OK")
        } else {
            exchange.sendResponseHeaders(500, 0)
            logger.info("Response 500")
        }
        exchange.close()
    }
}