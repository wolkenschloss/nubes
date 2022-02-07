package wolkenschloss.domain;

import com.sun.net.httpserver.HttpServer;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.libvirt.LibvirtException;
import wolkenschloss.TestbedExtension;
import java.util.Optional;
import javax.annotation.Nonnull;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CacheableTask
abstract public class BuildDomain extends DefaultTask {

    @Input
    abstract public Property<String> getDomain();

    @Internal
    abstract public Property<Integer> getPort();

    @Input
    public abstract ListProperty<String> getHosts();

    @Input
    public abstract Property<String> getDomainSuffix();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getXmlDescription();

    @Inject
    abstract protected ExecOperations getExecOperations();

    @OutputFile
    @Nonnull
    abstract public RegularFileProperty getKnownHostsFile();

    @OutputFile
    abstract public RegularFileProperty getHostsFile();

    @Internal
    abstract public Property<DomainOperations> getDomainOperations();

    @TaskAction
    public void exec() throws IOException, LibvirtException, InterruptedException {

        var knownHostsFile = getKnownHostsFile().getAsFile().get();

        if (knownHostsFile.exists()) {
            var message = String.format("File %s already exists. Destroy testbed with './gradlew :%s:destroy' before starting a new one",
                    knownHostsFile.getPath(),
                    getProject().getName());

            var extension = Optional.ofNullable(getProject().getExtensions().findByType(TestbedExtension.class));

            var failOnError = extension.map(ext -> ext.getFailOnError().get()).orElse(true);

            if (failOnError) {
                throw new GradleException(message);
            }

            getLogger().warn(message);
            return;
        }

        DomainOperations domainOperations = getDomainOperations().get();
        String xml = Files.readString(getXmlDescription().get().getAsFile().toPath());
        domainOperations.create(xml);

        var serverKey = waitForCallback();
        updateKnownHosts(serverKey);
        updateHosts();
    }

    private void updateKnownHosts(String serverKey) throws IOException, LibvirtException, InterruptedException {
        getLogger().info("create known_hosts file");
        DomainOperations domainOperations = getDomainOperations().get();

        var ip = domainOperations.getIpAddress();

        var permissions = Set.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ);
        var attributes = PosixFilePermissions.asFileAttribute(permissions);
        var path = getKnownHostsFile().get().getAsFile().toPath();
        var file = Files.createFile(path, attributes);

        Files.writeString(
                file.toAbsolutePath(),
                String.format("%s %s%n", ip, serverKey),
                StandardOpenOption.WRITE);

        Files.writeString(
                file.toAbsolutePath(),
                String.format("%s.%s %s%n", getDomain().get(), getDomainSuffix().get(), serverKey),
                StandardOpenOption.APPEND);
    }

    private void updateHosts() throws IOException, LibvirtException, InterruptedException {
        getLogger().info("create hosts file");

        var domainOperations = getDomainOperations().get();
        var ip = domainOperations.getIpAddress();
        var path = getHostsFile().get().getAsFile().toPath();
        var file = Files.createFile(path);

        this.getLogger().info("Writing hosts file: {}", path.toAbsolutePath());

        var hosts = Stream.concat(
                        Stream.of(ip),
                        Stream.concat(
                                        Stream.of(getDomain().get()),
                                        getHosts().get().stream())
                                .map(host -> String.format("%s.%s", host, getDomainSuffix().get())))
                .collect(Collectors.joining(" "));

        Files.writeString(file, hosts, StandardOpenOption.WRITE);
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
