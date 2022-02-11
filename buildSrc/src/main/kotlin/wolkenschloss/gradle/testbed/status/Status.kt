package wolkenschloss.gradle.testbed.status

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.libvirt.DomainInfo
import org.libvirt.StoragePool
import wolkenschloss.gradle.testbed.domain.DomainOperations
import wolkenschloss.gradle.testbed.domain.RegistryService
import wolkenschloss.gradle.testbed.domain.SecureShellService
import wolkenschloss.gradle.testbed.pool.PoolOperations
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import javax.inject.Inject

abstract class Status : DefaultTask() {
    @get:Internal
    abstract val domainName: Property<String>

    @get:Internal
    abstract val downloadDir: DirectoryProperty

    @get:Internal
    abstract val baseImageFile: RegularFileProperty

    @get:Internal
    abstract val knownHostsFile: RegularFileProperty

    @get:Internal
    abstract val kubeConfigFile: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Internal
    abstract val poolOperations: Property<PoolOperations>

    @get:Internal
    abstract val domainOperations: Property<DomainOperations>

    @TaskAction
    fun printStatus() {
        logger.quiet("Status of {}", domainName.get())
        val domainOperations = domainOperations.get()

        check<DomainOperations>(
            "Testbed",
            { method: Consumer<DomainOperations> -> domainOperations.withDomain<Consumer<DomainOperations>>(method) },
            { domain: DomainOperations ->
                info("IP Address") { domain.ipAddress }
                check("Secure Shell", domain.withShell(execOperations)) { shell: SecureShellService ->
                    check(
                        "ssh uname",
                        shell.withCommand("uname", "-norm")
                    ) { result: SecureShellService.Result ->
                        info("stdout") { result.stdout }
                        info("exit code") { result.exitValue }
                    }
                }
                check<DomainInfo>(
                    "Connect",
                    { consumer: Consumer<DomainInfo> -> domain.withInfo(consumer) }) { info: DomainInfo ->
                    check(
                        "State",
                        info.state
                    ) { s: DomainInfo.DomainState -> s == DomainInfo.DomainState.VIR_DOMAIN_RUNNING }
                    check("Memory (MB)", info.memory / 1024) { m: Long -> m >= 4096 }
                    check("Virtual CPU's", info.nrVirtCpu) { n: Int -> n > 1 }
                }
                evaluate2("K8s config", { kubeConfigFile.asFile.get().toPath() },
                    { status: Check<Path> ->
                        status.check { path: Path -> Files.exists(path) }
                            .ok { obj: Path -> obj.toString() }
                            .error("missing")
                    })
                val poolOperations: PoolOperations = poolOperations.get()
                check<StoragePool>(
                    "Pool",
                    { consumer: Consumer<StoragePool> -> poolOperations.run(consumer) }) { p: StoragePool ->
                    info("Pool Name") { p.name }
                    info("Pool Autostart") { p.autostart }
                    info("Pool isActive") { p.isActive }
                    evaluate2("Pool Volumes", { p.listVolumes() },
                        { status: Check<Array<String>> ->
                            status.check { vols: Array<String> ->
                                listOf(*vols).contains("root.qcow2") && listOf(*vols).contains("cidata.img")
                            }
                                .ok(Function { vols: Array<String> -> java.lang.String.join(", ", *vols) })
                                .error("Nicht genau drei Volumes")
                        })
                }
                val registryService: RegistryService = domain.registry
                check<RegistryService>(
                    "Registry",
                    { method: Consumer<RegistryService> ->
                        registryService.withRegistry(method)
                    },
                    { registry: RegistryService ->
                        info("Address") { registry.address }
                        info("Upload Image") { registry.uploadImage("hello-world:latest") }
                        evaluate2("Catalogs", { registry.listCatalogs() },
                            { status ->
                                status.check(
                                    Predicate { catalogs: List<String> -> catalogs.contains("hello-world") })
                                    .ok(Function { catalogs: List<String> -> java.lang.String.join(", ", catalogs) })
                                    .error("missing catalog hello-world")
                            }
                        )
                    })
            })
        info("XDG_DATA_HOME") { downloadDir.get().asFile.toPath() }
        evaluate2("Base image", { baseImageFile.asFile.get().toPath() }
        ) { status ->
            status.check { path -> Files.exists(path) }
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

    private fun <T> check(label: String, fn: Consumer<Consumer<T>>, with: Consumer<T>) {
        try {
            fn.accept(with)
            logger.quiet(String.format("✓ %-15s: %s", label, "OK"))
        } catch (e: Throwable) {
            logger.error(String.format("✗ %-15s: %s", label, e.message))
        }
    }

    private fun <T> evaluate2(
        label: String,
        fn: () -> T,
        check: Function<Check<T>, StatusChecker>
    ) {
        try {
            val builder = StatusBuilder(this, fn)
            check.apply(builder).run(label)
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