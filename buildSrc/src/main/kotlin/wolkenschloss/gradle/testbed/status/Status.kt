package wolkenschloss.gradle.testbed.status

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import wolkenschloss.gradle.testbed.domain.DomainOperations
import wolkenschloss.gradle.testbed.domain.RegistryService
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
        val domainOperations = DomainOperations(execOperations, domainName)

        info("IP Address") { domainOperations.ipAddress() }

        check("Testbed") {

            check("K8s config", { kubeConfigFile.asFile.get().toPath() }) {
                check { path: Path -> Files.exists(path) }
                    .ok { path -> path.toString() }
                    .error("missing")
            }
        }

        val registryService = RegistryService(registry.get(), truststore)

        check("Registry")
        {
            info("Address") { registryService.name }
            info("Upload Image") { registryService.push("hello-world:latest", "hello-world:latest") }
            check("Catalogs", { registryService.listCatalogs(certificate) }) {
                check { it.contains("hello-world") }
                    .ok { java.lang.String.join(", ", it) }
                    .error("missing catalog hello-world")
            }
        }

        check("TLS Secrets") {
            domainOperations.readAllTlsSecrets()
                .forEach {
                    check(it.toString(), {it} ) {
                        check {it.certificate.isValid()}
                            .ok { "Valid until ${it.certificate.notAfter}"}
                            .error("Expired at ${it.certificate.notAfter}")
                    }
                }
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