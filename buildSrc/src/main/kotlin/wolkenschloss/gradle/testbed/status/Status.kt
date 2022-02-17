package wolkenschloss.gradle.testbed.status

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.libvirt.DomainInfo
import wolkenschloss.gradle.testbed.domain.DomainOperations
import wolkenschloss.gradle.testbed.domain.RegistryService
import wolkenschloss.gradle.testbed.pool.PoolOperations
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Predicate
import javax.inject.Inject

abstract class Status : DefaultTask() {
    @get:Internal
    abstract val domainName: Property<String>

    @get:Internal
    abstract val registry: Property<String>

    @get:Internal
    abstract val poolName: Property<String>

    @get:Internal
    abstract val downloadDir: DirectoryProperty

    @get:Internal
    abstract val baseImageFile: RegularFileProperty

    @get:Internal
    abstract val knownHostsFile: RegularFileProperty

    @get:Internal
    abstract val kubeConfigFile: RegularFileProperty

    @get:Internal
    abstract val certificate: RegularFileProperty

    @get:Internal
    abstract val truststore: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun printStatus() {
        logger.quiet("Status of {}", domainName.get())
        val domainOperations = DomainOperations.getInstance(project.gradle).get()

        check("Testbed") {
            domainOperations.withDomain(domainName) { domain ->
                info("IP Address") { domain.ipAddress(domainName) }
                check("Secure Shell") {
                    domain.withShell(domainName, knownHostsFile, execOperations) { shell ->
                        check("ssh uname") {
                            shell.withCommand(listOf("uname", "-norm")) { result ->
                                info("stdout") { result.stdout }
                                info("exit code") { result.exitValue }
                            }
                        }
                    }
                }

                check("Connect") {
                    domain.withInfo(domainName) { info: DomainInfo ->
                        check("State", info.state) { s -> s == DomainInfo.DomainState.VIR_DOMAIN_RUNNING }
                        check("Memory (MB)", info.memory / 1024) { m: Long -> m >= 4096 }
                        check("Virtual CPU's", info.nrVirtCpu) { n: Int -> n > 1 }
                    }
                }

                check("K8s config", { kubeConfigFile.asFile.get().toPath() }) {
                    check { path: Path -> Files.exists(path) }
                        .ok { path -> path.toString() }
                        .error("missing")
                }
            }
        }
        val registryService = RegistryService(registry.get(), truststore)

        check("Registry") {
            info("Address") { registryService.name }
            info("Upload Image") { registryService.push("hello-world:latest", "hello-world:latest") }
            check("Catalogs", { registryService.listCatalogs(certificate) }) {
                check { it.contains("hello-world") }
                    .ok { java.lang.String.join(", ", it) }
                    .error("missing catalog hello-world")
            }
        }

        check("Pool") {
            val poolOperations = PoolOperations.getInstance(project.gradle).get()
            poolOperations.run(poolName.get()) { p ->
                info("Pool Name") { p.name }
                info("Pool Autostart") { p.autostart }
                info("Pool isActive") { p.isActive }
                check("Pool Volumes", { p.listVolumes() }) {
                    check { vols -> vols.contains("root.qcow2") && vols.contains("cidata.img")}
                        .ok { vols -> java.lang.String.join(", ", *vols) }
                        .error("Nicht genau zwei Volumes")
                }
            }
        }

        info("Download Directory") { downloadDir.get().asFile.toPath() }
        check("Base image", { baseImageFile.asFile.get().toPath() }) {
            check { path -> Files.exists(path) }
                .ok(Path::toString)
                .error("missing")
        }
    }

    private fun <T> check(label: String, value: T, requiredCondition: Predicate<T>) {
        if (requiredCondition.test(value)) {
            logger.quiet(String.format("✓ %-15s: %s", label, value))
        } else {
            logger.error(String.format("✗ %-15s: %s", label, value))
        }
    }

    private fun check(label: String, fn: () -> Unit) {
        try {
            fn()
            logger.quiet(String.format("✓ %-15s: %s", label, "OK"))
        } catch (e: Throwable) {
            logger.error(String.format("✗ %-15s: %s", label, e.message))
        }
    }

    private fun <T> check(
        label: String,
        fn: () -> T,
        check: Check<T>.() -> StatusChecker
    ) {
        try {
            val builder = StatusBuilder(this, fn)
            check(builder).run(label)
        } catch (e: Throwable) {
            logger.error(String.format("✗ %-15s: %s", label, e.message))
        }
    }

    private fun <T> info(label: String, fn: CheckedSupplier<T>) {
        try {
            logger.quiet(String.format("✓ %-15s: %s", label, fn.apply()))
        } catch (e: Throwable) {
            logger.error(String.format("✗ %-15s: %s", label, e.message))
        }
    }
}