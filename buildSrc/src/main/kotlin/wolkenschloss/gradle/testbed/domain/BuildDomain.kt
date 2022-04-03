package wolkenschloss.gradle.testbed.domain

import com.jayway.jsonpath.JsonPath
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.inject.Inject

abstract class BuildDomain : DefaultTask() {

    @get:Input
    abstract val domain: Property<String>

    @get:InputFile
    abstract val userData: Property<File>

    @get:Input
    abstract val hosts: ListProperty<String>

    @get:Input
    abstract val domainSuffix: Property<String>

    @get:Input
    abstract val staticIp: Property<String>

    @get:OutputFile
    abstract val hostsFile: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations


    data class Result(val output: String, val exitCode: Int);

    private fun execute(project: Project, action: ExecSpec.() -> Unit): Result {
        ByteArrayOutputStream().use {
            val result = project.exec {
                standardOutput = it
                action(this)
            }
            return Result(it.toString(), result.exitValue)
        }
    }

    private fun execute(project: Project, input: Property<File>, action: ExecSpec.() -> Unit): Result {
        input.get().inputStream().use { inputStream ->
            ByteArrayOutputStream().use {
                val result = project.exec {
                    standardOutput = it
                    standardInput = inputStream
                    action(this)
                }
                return Result(it.toString(), result.exitValue)
            }
        }
    }

    @TaskAction
    fun exec() {
//        if (launch()) return
        launch()

//        val knownHostsFile = knownHostsFile.asFile.get()
//        if (knownHostsFile.exists()) {
//            val extension: Optional<TestbedExtension> = Optional.ofNullable(
//                project.extensions.findByType(TestbedExtension::class.java))
//
//            val failOnError = extension
//                .map { ext: TestbedExtension -> ext.failOnError.get()}
//                .orElse(true)
//
//            return
//        }

        updateHosts()
    }

    private fun launch(): Boolean {
        // TODO: Existiert die Testbed Instanz?
        logger.quiet("Prüfe, ob Instanz existiert")
        val r1 = execute(project) {
            commandLine("multipass", "info", "--format", "json", domain.get())
            isIgnoreExitValue = true
        }

        if (r1.exitCode == 0) {
            val path = "\$.info.testbed.state"
            val state: String = JsonPath.parse(r1.output).read(path)
            if (state == "Running") {
                logger.quiet("Die Instanz läuft bereits")
                return true
            } else {
                logger.quiet("Die Instanz wird gestartet")
                execute(project) {
                    commandLine("multipass", "start", domain.get())
                }
                return true
            }
        }

        logger.quiet("Instanz wird erzeugt")
        // Erzeuge:
        execute(project, userData) {
            commandLine(
                "multipass", "launch",
                "--cpus", "2",
                "--disk", "20G",
                "--mem", "4G",
                "--name", domain.get(),
                "--timeout", "900",
                "--cloud-init", "-"
            )
        }
        return false
    }

    private fun updateHosts() {
        logger.info("create hosts file")


        val ip = DomainOperations(execOperations, domain).ipAddress() //staticIp.get()
        val path = hostsFile.get().asFile

        if (path.exists()) {
            path.delete()
        }

        logger.info("Writing hosts file: {}", path.absolutePath)

        val hosts = Stream.concat(
            Stream.of(ip),
            Stream.concat(
                Stream.of(domain.get()),
                hosts.get().stream()
            )
                .map { host: String? -> String.format("%s.%s", host, domainSuffix.get()) })
            .collect(Collectors.joining(" ")) + "\n"

        path.writeText(hosts)
    }
}