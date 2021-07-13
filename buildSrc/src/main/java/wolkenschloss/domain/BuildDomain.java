package wolkenschloss.domain;

import com.sun.net.httpserver.HttpServer;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.libvirt.LibvirtException;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

@CacheableTask
abstract public class BuildDomain extends DefaultTask {

    @Input
    abstract public Property<String> getDomain();

    @Internal
    abstract public Property<Integer> getPort();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getXmlDescription();

    @Inject
    abstract protected ExecOperations getExecOperations();

    @OutputFile
    abstract public RegularFileProperty getKnownHostsFile();

    @Internal
    abstract public Property<DomainOperations> getDomainOperations();

    @TaskAction
    public void exec() throws IOException, LibvirtException, InterruptedException {

        var knownHostsFile = getKnownHostsFile().getAsFile().get();

        if (knownHostsFile.exists()) {
            var message = String.format("File %s already exists. Destroy testbed with './gradlew :%s:destroy' before starting a new one",
                    knownHostsFile.getPath(),
                    getProject().getName());

            throw new GradleException(message);
        }

        DomainOperations domainOperations = getDomainOperations().get();
        String xml = Files.readString(getXmlDescription().get().getAsFile().toPath());
        domainOperations.create(xml);

        var serverKey = waitForCallback();
        updateKnownHosts(serverKey);
    }

    private void updateKnownHosts(String serverKey) throws IOException, LibvirtException, InterruptedException {
        DomainOperations domainOperations = getDomainOperations().get();

        var ip = domainOperations.getIpAddress();

        var permissions = Set.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ);
        var attributes = PosixFilePermissions.asFileAttribute(permissions);
        var path = getKnownHostsFile().get().getAsFile().toPath();
        var file = Files.createFile(path, attributes);

        Files.writeString(
                file.toAbsolutePath(),
                String.format("%s %s", ip, serverKey),
                StandardOpenOption.WRITE);
    }

    private String waitForCallback() {
        try {
            var executor = Executors.newSingleThreadExecutor();
            BlockingQueue<String> serverKeyResult = new SynchronousQueue<>();

            var server = HttpServer.create(new InetSocketAddress(getPort().get()), 0);

            server.createContext(
                    String.format("/%s", getDomain().get()),
                    new CallbackHandler(serverKeyResult, getLogger()));

            server.setExecutor(executor);
            server.start();
            getLogger().lifecycle("Waiting for connection from testbed");

            try {
                var serverKey = serverKeyResult.poll(10, TimeUnit.MINUTES);

                if (serverKey != null) {
                    return serverKey;
                } else {
                    throw new GradleException("Did not receive call from testbed");
                }
            } catch (InterruptedException exception) {
                throw new GradleException("Premature termination while waiting for the callback");
            } finally {
                executor.shutdown();
                server.stop(0);
            }
        } catch (IOException e) {
            throw new GradleScriptException("Can not start webserver", e);
        }
    }
}
