package wolkenschloss.gradle.testbed.domain

import com.sun.net.httpserver.HttpServer
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.GradleScriptException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.libvirt.LibvirtException
// TODO: no upward dependencies
import wolkenschloss.gradle.testbed.TestbedExtension
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.Optional
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import java.util.stream.Stream

@CacheableTask
abstract class BuildDomain : DefaultTask() {
    @get:Input
    abstract val domain: Property<String?>

    @get:Internal
    abstract val port: Property<Int?>

    @get:Input
    abstract val hosts: ListProperty<String>

    @get:Input
    abstract val domainSuffix: Property<String?>

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val xmlDescription: Property<File>

    @get:OutputFile
    abstract val knownHostsFile: RegularFileProperty

    @get:OutputFile
    abstract val hostsFile: RegularFileProperty

    @get:Internal
    abstract val domainOperations: Property<DomainOperations?>
    @TaskAction
    @Throws(IOException::class, LibvirtException::class, InterruptedException::class)
    fun exec() {
        val knownHostsFile = knownHostsFile.asFile.get()
        if (knownHostsFile.exists()) {
            val message = String.format(
                "File %s already exists. Destroy testbed with './gradlew :%s:destroy' before starting a new one",
                knownHostsFile.path,
                project.name
            )
            val extension: Optional<TestbedExtension> = Optional.ofNullable(
                project.extensions.findByType(TestbedExtension::class.java))

            val failOnError = extension
                .map { ext: TestbedExtension -> ext.failOnError.get()}
                .orElse(true)

            if (failOnError) {
                throw GradleException(message)
            }

            logger.warn(message)
            return
        }
        val domainOperations = domainOperations.get()
        val xml = xmlDescription.get().readText()
        domainOperations.create(xml)
        val serverKey = waitForCallback()
        updateKnownHosts(serverKey)
        updateHosts()
    }

    @Throws(IOException::class, LibvirtException::class, InterruptedException::class)
    private fun updateKnownHosts(serverKey: String) {
        logger.info("create known_hosts file")
        val domainOperations = domainOperations.get()
        val ip = domainOperations.ipAddress
        val permissions = setOf(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ)
        val attributes = PosixFilePermissions.asFileAttribute(permissions)
        val path = knownHostsFile.get().asFile.toPath()
        val file = Files.createFile(path, attributes)
        Files.writeString(
            file.toAbsolutePath(), String.format("%s %s%n", ip, serverKey),
            StandardOpenOption.WRITE
        )
        Files.writeString(
            file.toAbsolutePath(), String.format("%s.%s %s%n", domain.get(), domainSuffix.get(), serverKey),
            StandardOpenOption.APPEND
        )
    }

    @Throws(IOException::class, LibvirtException::class, InterruptedException::class)
    private fun updateHosts() {
        logger.info("create hosts file")
        val domainOperations = domainOperations.get()
        val ip = domainOperations.ipAddress
        val path = hostsFile.get().asFile.toPath()
        val file = Files.createFile(path)
        logger.info("Writing hosts file: {}", path.toAbsolutePath())
        val hosts = Stream.concat(
            Stream.of(ip),
            Stream.concat(
                Stream.of(domain.get()),
                hosts.get().stream()
            )
                .map { host: String? -> String.format("%s.%s", host, domainSuffix.get()) })
            .collect(Collectors.joining(" "))
        Files.writeString(file, hosts, StandardOpenOption.WRITE)
    }

    private fun waitForCallback(): String {
        return try {
            val executor = Executors.newSingleThreadExecutor()
            val serverKeyResult: BlockingQueue<String> = SynchronousQueue()
            val server = HttpServer.create(
                InetSocketAddress(
                    port.get()
                ), 0
            )
            server.createContext(
                String.format("/%s", domain.get()),
                CallbackHandler(serverKeyResult, logger)
            )
            server.executor = executor
            server.start()
            logger.lifecycle("Waiting for connection from testbed")
            try {
                val serverKey = serverKeyResult.poll(10, TimeUnit.MINUTES)
                serverKey ?: throw GradleException("Did not receive call from testbed")
            } catch (exception: InterruptedException) {
                throw GradleException("Premature termination while waiting for the callback")
            } finally {
                executor.shutdown()
                server.stop(0)
            }
        } catch (e: IOException) {
            throw GradleScriptException("Can not start webserver", e)
        }
    }
}