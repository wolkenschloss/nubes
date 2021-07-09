package wolkenschloss.task.start;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class CallbackHandler implements HttpHandler {
    private final BlockingQueue<String> server;
    private final Logger logger;

    public CallbackHandler(BlockingQueue<String> server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        var data = new FormData(body);
        var serverKey = data.value("pub_key_ecdsa");

        if (server.offer(serverKey)) {
            exchange.sendResponseHeaders(200, 0);
            logger.info("Response OK");
        } else {
            exchange.sendResponseHeaders(500, 0);
            logger.info("Response 500");
        }

        exchange.close();
    }

}
