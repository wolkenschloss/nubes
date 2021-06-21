package wolkenschloss;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.*;

/**
 * Die Aufgabe wartet auf einen eingehenden Anruf des Prüfstandes.
 *
 * Der Prüfstand führt einen HTTP Anfrage aus. Diese Aufgabe startet
 * einen Webserver, der geeignet konfiguriert ist, um die HTTP Anfrage
 * des Prüfstandes entgegenzunehmen.
 *
 * Falls der Prozess hängen bleibt: sudo netstat -plten | grep java
 *
 * TODO: Falls gradle mit Ctrl-C abgebrochen wird, sollte der Server
 * herunterfahren und den Port freigeben.
 */
public abstract class WaitForCallTask extends DefaultTask {

    @Input
    abstract public Property<Integer> getPort();

    @Input
    abstract public Property<String> getHostname();

    @OutputFile
    abstract public RegularFileProperty getServerKey();

    final BlockingQueue<String> serverKeyResult;

    public WaitForCallTask() {
        this.serverKeyResult = new SynchronousQueue<>();
    }

    @TaskAction
    public void run() {
        try {
            var executor = Executors.newSingleThreadExecutor();

            var server = HttpServer.create(new InetSocketAddress(getPort().get()), 0);
            server.createContext(String.format("/%s", getHostname().get()), new TestbedCallbackHandler(serverKeyResult, getLogger()));

            server.setExecutor(executor);
            server.start();
            getLogger().info("Waiting for connection from testbed");

            try {
                var serverKey = serverKeyResult.poll(10, TimeUnit.MINUTES);

                if (serverKey != null) {
                    var keyFile = getServerKey().get().getAsFile();
                    if(keyFile.getParentFile().mkdirs()) {
                        getLogger().info("Directory {} created", keyFile.getParentFile().getAbsolutePath());
                    }
                    Files.writeString(keyFile.toPath(), serverKey);
                } else {
                    throw new GradleException("Did not receive call from testbed");
                }

            } catch (InterruptedException exception) {
                getLogger().error("Premature termination while waiting for the callback");
            }
            finally {
                executor.shutdown();
                server.stop(0);
            }

        } catch (IOException e) {
            throw new GradleScriptException("Can not start webserver", e);
        }
    }

    static class TestbedCallbackHandler implements HttpHandler {
        private final BlockingQueue<String> server;
        private final Logger logger;

        public TestbedCallbackHandler(BlockingQueue<String> server, Logger logger) {
            this.server = server;
            this.logger = logger;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            logger.info("Get Request {}", exchange.getRequestMethod());
            logger.info("Before shutdown");

            var body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            logger.info(">>> BODY: {}", body);
            var data = new FormData(body);
            var serverKey = data.value("pub_key_ecdsa");
            logger.info("Got Server Key: {}", serverKey);

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
}
